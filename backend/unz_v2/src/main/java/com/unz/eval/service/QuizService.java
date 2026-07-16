package com.unz.eval.service;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.notification.NotificationService;
import com.unz.eval.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepo;
    private final QuestionRepository questionRepo;
    private final QuizQuestionRepository qqRepo;
    private final TentativeRepository tentativeRepo;
    private final ReponseRepository reponseRepo;
    private final MatiereRepository matiereRepo;
    private final ClasseRepository classeRepo;
    private final InscriptionRepository inscriptionRepo;
    private final AnneeAcademiqueService anneeService;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final NoteRepository noteRepo;
    private final TentativeEvenementRepository evenementRepo;

    public QuizService(QuizRepository quizRepo, QuestionRepository questionRepo, QuizQuestionRepository qqRepo, TentativeRepository tentativeRepo, ReponseRepository reponseRepo, MatiereRepository matiereRepo, ClasseRepository classeRepo, InscriptionRepository inscriptionRepo, AnneeAcademiqueService anneeService, UserRepository userRepo, NotificationService notificationService, NoteRepository noteRepo, TentativeEvenementRepository evenementRepo) {
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.qqRepo = qqRepo;
        this.tentativeRepo = tentativeRepo;
        this.reponseRepo = reponseRepo;
        this.matiereRepo = matiereRepo;
        this.classeRepo = classeRepo;
        this.inscriptionRepo = inscriptionRepo;
        this.anneeService = anneeService;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.noteRepo = noteRepo;
        this.evenementRepo = evenementRepo;
    }

    /**
     * v3 : la classe d'un étudiant dépend désormais de son Inscription pour
     * l'année académique active (remplace l'ancien user.getClasse() direct).
     */
    private Classe classeCourante(User etudiant) {
        AnneeAcademique annee = anneeService.getAnneeActive();
        Inscription inscription = inscriptionRepo.findByEtudiantAndAnneeAcademique(etudiant, annee)
                .orElseThrow(() -> new BadRequestException(
                        "Vous n'êtes inscrit dans aucune classe pour l'année académique en cours."));
        return inscription.getClasse();
    }

    /** EF-2 : l'étudiant ne voit que les quiz de sa classe courante */
    public List<DTOs.QuizListDTO> getQuizzesForUser(User user) {
        List<Quiz> quizzes = user.isEtudiant()
                ? quizRepo.findByClasse(classeCourante(user))
                : quizRepo.findByCreePar(user);
        return quizzes.stream().map(this::toListDTO).collect(Collectors.toList());
    }

    public DTOs.QuizDetailDTO getQuizDetail(Long quizId, User user) {
        Quiz quiz = findQuizOrThrow(quizId);
        // ENF-2 : un étudiant ne peut voir que les quiz de sa classe courante
        if (user.isEtudiant() && !quiz.getClasse().getId().equals(classeCourante(user).getId()))
            throw new AccessDeniedException("Ce quiz ne concerne pas votre classe.");
        return toDetailDTO(quiz, user);
    }

    @Transactional
    public Quiz createQuiz(DTOs.CreateQuizRequest req, User enseignant) {
        Matiere matiere = matiereRepo.findById(req.getMatiereId())
                .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        Classe classe = classeRepo.findById(req.getClasseId())
                .orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));

        if (!matiere.estEnseignePar(enseignant))
            throw new AccessDeniedException("Vous ne pouvez créer un quiz que pour vos matières.");

        Quiz quiz = Quiz.builder()
                .titre(req.getTitre())
                .typeQuiz(Quiz.TypeQuiz.valueOf(req.getTypeQuiz()))
                .noteSur(req.getNoteSur())
                .dureeMinutes(req.getDureeMinutes())
                .tentativesMax(req.getTentativesMax())
                .delaiEntreTentativesMinutes(req.getDelaiEntreTentativesMinutes())
                .dateOuverture(req.getDateOuverture())
                .dateCloture(req.getDateCloture())
                .matiere(matiere).classe(classe).creePar(enseignant)
                .build();
        quiz = quizRepo.save(quiz);

        // Notifie les étudiants de la classe si le quiz est déjà ouvert
        boolean dejaOuvert = quiz.getDateOuverture() == null
                || !quiz.getDateOuverture().isAfter(LocalDateTime.now());
        if (dejaOuvert) {
            List<User> etudiants = userRepo.findByInscriptions_Classe(classe);
            notificationService.notifierQuizOuvert(etudiants, quiz.getTitre(), quiz.getId());
        }
        return quiz;
    }

    @Transactional
    public void addQuestions(Long quizId, List<Long> questionIds, User enseignant) {
        Quiz quiz = findQuizOrThrow(quizId);
        if (!quiz.getCreePar().getId().equals(enseignant.getId()))
            throw new AccessDeniedException("Vous ne pouvez modifier que vos quiz.");

        int ordre = qqRepo.countByQuiz(quiz);
        for (Long qId : questionIds) {
            Question q = questionRepo.findById(qId)
                    .orElseThrow(() -> new ResourceNotFoundException("Question introuvable : " + qId));
            if (!q.getMatiere().getId().equals(quiz.getMatiere().getId()))
                throw new BadRequestException("La question n'appartient pas à la matière du quiz.");
            if (!qqRepo.existsByQuizAndQuestion(quiz, q)) {
                qqRepo.save(QuizQuestion.builder().quiz(quiz).question(q).ordre(ordre).build());
            }
        }
    }

    /**
     * EF-10 : clôture explicite — rend les notes visibles.
     * EF-18 : pour un devoir de type EXAMEN, chaque tentative soumise génère
     * désormais une Note officielle rattachée à la matière.
     */
    @Transactional
    public void cloturerQuiz(Long quizId, User enseignant) {
        Quiz quiz = findQuizOrThrow(quizId);
        if (!quiz.getCreePar().getId().equals(enseignant.getId()))
            throw new AccessDeniedException("Vous ne pouvez clôturer que vos quiz.");
        if (quiz.isClotureValideeParEnseignant())
            throw new BadRequestException("Ce quiz est déjà clôturé.");

        List<Tentative> tentativesSoumises = tentativeRepo.findByQuizAndDateSoumissionIsNotNull(quiz);
        tentativesSoumises.forEach(t -> {
            if (t.getNoteObtenue() == null) { t.corriger(); tentativeRepo.save(t); }
        });

        if (quiz.getTypeQuiz() == Quiz.TypeQuiz.EXAMEN) {
            for (Tentative t : tentativesSoumises) {
                Note note = Note.builder()
                        .etudiant(t.getEtudiant())
                        .matiere(quiz.getMatiere())
                        .typeNote(Note.TypeNote.QUIZ)
                        .valeur(java.math.BigDecimal.valueOf(t.getNoteObtenueSur20())
                                .setScale(2, java.math.RoundingMode.HALF_UP))
                        .ponderation(java.math.BigDecimal.ONE)
                        .saisieParr(enseignant)
                        .dateSaisie(java.time.LocalDateTime.now())
                        .build();
                noteRepo.save(note);
            }
        }

        quiz.setClotureValideeParEnseignant(true);
        quizRepo.save(quiz);

        List<User> etudiants = userRepo.findByInscriptions_Classe(quiz.getClasse());
        notificationService.notifierNotesCloture(etudiants, quiz.getTitre(), quiz.getId());
    }

    /**
     * Supprime un devoir/quiz. Uniquement possible tant qu'aucun étudiant
     * n'a commencé de tentative dessus (sinon on perdrait des données de
     * réponses/notes déjà soumises).
     */
    @Transactional
    public void supprimerQuiz(Long quizId, User enseignant) {
        Quiz quiz = findQuizOrThrow(quizId);
        if (!quiz.getCreePar().getId().equals(enseignant.getId()))
            throw new AccessDeniedException("Vous ne pouvez supprimer que vos propres devoirs.");
        if (tentativeRepo.existsByQuiz(quiz))
            throw new BadRequestException("Impossible de supprimer un devoir déjà commencé par des étudiants.");
        quizRepo.delete(quiz);
    }

    // ── Tentatives ────────────────────────────────────────────────────────

    /**
     * EF-5 / EF-16 : démarre une tentative avec toutes les validations.
     * v3.2 : si une tentative non soumise existe déjà (rechargement de page,
     * changement d'appareil...), on la reprend au lieu de bloquer l'étudiant
     * ou d'en créer une nouvelle — corrige un vrai risque de blocage définitif.
     */
    @Transactional
    public Tentative demarrerTentative(Long quizId, User etudiant) {
        Quiz quiz = findQuizOrThrow(quizId);

        if (quiz.isEstCloture())
            throw new BadRequestException("Ce quiz est clôturé.");
        if (!quiz.getClasse().getId().equals(classeCourante(etudiant).getId()))
            throw new AccessDeniedException("Ce quiz ne concerne pas votre classe.");

        List<Tentative> existantes = tentativeRepo.findByQuizAndEtudiant(quiz, etudiant);

        Optional<Tentative> enCours = existantes.stream()
                .filter(t -> t.getDateSoumission() == null)
                .findFirst();
        if (enCours.isPresent()) return enCours.get();

        long soumises = existantes.stream().filter(t -> t.getDateSoumission() != null).count();

        // EF-5 : EXAMEN = 1 seule tentative soumise
        if (quiz.getTypeQuiz() == Quiz.TypeQuiz.EXAMEN && soumises > 0)
            throw new BadRequestException("Vous avez déjà passé cet examen (tentative unique).");

        // EF-16 : ENTRAINEMENT = vérifier le max et le délai
        if (quiz.getTypeQuiz() == Quiz.TypeQuiz.ENTRAINEMENT) {
            if (soumises >= quiz.getTentativesMax())
                throw new BadRequestException("Nombre maximum de tentatives atteint (" + quiz.getTentativesMax() + ").");

            Optional<Tentative> derniere = tentativeRepo
                    .findTopByQuizAndEtudiantOrderByDateDebutDesc(quiz, etudiant);
            if (derniere.isPresent() && quiz.getDelaiEntreTentativesMinutes() > 0) {
                LocalDateTime prochaineDispo = derniere.get().getDateDebut()
                        .plusMinutes(quiz.getDelaiEntreTentativesMinutes());
                if (LocalDateTime.now().isBefore(prochaineDispo))
                    throw new BadRequestException("Veuillez attendre avant de repasser ce quiz. Disponible à : " + prochaineDispo);
            }
        }

        Tentative t = Tentative.builder().quiz(quiz).etudiant(etudiant).build();
        return tentativeRepo.save(t);
    }

    /**
     * v3.2 : sauvegarde immédiate d'UNE réponse (autosave). Peut être appelée
     * autant de fois que nécessaire pendant l'épreuve — corrige l'absence de
     * sauvegarde progressive (perte totale des réponses en cas de rechargement).
     */
    @Transactional
    public void sauvegarderReponse(Long tentativeId, DTOs.SubmitReponseRequest req, User etudiant) {
        Tentative tentative = tentativeRepo.findById(tentativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative introuvable"));
        if (!tentative.getEtudiant().getId().equals(etudiant.getId()))
            throw new AccessDeniedException("Cette tentative ne vous appartient pas.");
        if (tentative.getDateSoumission() != null)
            throw new BadRequestException("Cette tentative a déjà été soumise.");

        Question q = questionRepo.findById(req.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question introuvable"));

        Reponse reponse = reponseRepo.findByTentativeAndQuestion(tentative, q)
                .orElse(Reponse.builder().tentative(tentative).question(q).build());

        if (q.getType() == Question.TypeQuestion.REPONSE_COURTE) {
            reponse.setReponseTexte(req.getReponseTexte());
        } else {
            Set<Choix> choixSet = req.getChoixIds() == null ? Set.of() : q.getChoix().stream()
                    .filter(c -> req.getChoixIds().contains(c.getId()))
                    .collect(Collectors.toSet());
            reponse.setChoixSelectionnes(choixSet);
        }
        reponseRepo.save(reponse);
    }

    /**
     * v3.3 : anti-triche — enregistre un événement suspect signalé par le client
     * (changement d'onglet, perte de focus, copier-coller…) pendant une tentative en cours.
     * N'échoue pas bruyamment une fois la tentative soumise : l'événement est simplement ignoré,
     * pour ne jamais bloquer la fin d'un devoir à cause d'un signal tardif.
     */
    @Transactional
    public void signalerEvenement(Long tentativeId, String type, User etudiant) {
        Tentative tentative = tentativeRepo.findById(tentativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative introuvable"));
        if (!tentative.getEtudiant().getId().equals(etudiant.getId()))
            throw new AccessDeniedException("Cette tentative ne vous appartient pas.");
        if (tentative.getDateSoumission() != null) return;

        TentativeEvenement.Type t;
        try {
            t = TentativeEvenement.Type.valueOf(type);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BadRequestException("Type d'événement inconnu.");
        }

        TentativeEvenement ev = new TentativeEvenement();
        ev.setTentative(tentative);
        ev.setType(t);
        evenementRepo.save(ev);
    }

    /** v3.2 : reprise d'une tentative en cours — restaure l'heure de début réelle et les réponses déjà données */
    public DTOs.TentativeEnCoursDTO getTentativeEnCours(Long tentativeId, User etudiant) {
        Tentative tentative = tentativeRepo.findById(tentativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative introuvable"));
        if (!tentative.getEtudiant().getId().equals(etudiant.getId()))
            throw new AccessDeniedException("Cette tentative ne vous appartient pas.");

        List<DTOs.SubmitReponseRequest> reponses = reponseRepo.findByTentative(tentative).stream()
                .map(r -> {
                    DTOs.SubmitReponseRequest dto = new DTOs.SubmitReponseRequest();
                    dto.setQuestionId(r.getQuestion().getId());
                    dto.setChoixIds(r.getChoixSelectionnes() == null ? List.of()
                            : r.getChoixSelectionnes().stream().map(Choix::getId).collect(Collectors.toList()));
                    dto.setReponseTexte(r.getReponseTexte());
                    return dto;
                }).collect(Collectors.toList());

        return DTOs.TentativeEnCoursDTO.builder()
                .id(tentative.getId())
                .quizId(tentative.getQuiz().getId())
                .dateDebut(tentative.getDateDebut())
                .dureeMinutes(tentative.getQuiz().getDureeMinutes())
                .reponses(reponses)
                .build();
    }

    /** EF-9 : finalise la tentative et déclenche la correction automatique (les réponses sont déjà sauvegardées) */
    @Transactional
    public Tentative soumettreTentative(Long tentativeId, User etudiant) {
        Tentative tentative = tentativeRepo.findById(tentativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tentative introuvable"));

        if (!tentative.getEtudiant().getId().equals(etudiant.getId()))
            throw new AccessDeniedException("Cette tentative ne vous appartient pas.");
        if (tentative.getDateSoumission() != null)
            throw new BadRequestException("Cette tentative a déjà été soumise.");

        // v3.3 fix : avec orphanRemoval=true, on ne peut pas remplacer la collection
        // par une nouvelle instance — on vide puis on remplit la même référence
        List<Reponse> reponsesEnBase = reponseRepo.findByTentative(tentative);
        if (tentative.getReponses() == null) {
            tentative.setReponses(new java.util.ArrayList<>());
        }
        tentative.getReponses().clear();
        tentative.getReponses().addAll(reponsesEnBase);
        tentative.corriger();
        return tentativeRepo.save(tentative);
    }

    // ── DTO helpers ────────────────────────────────────────────────────────

    private DTOs.QuizListDTO toListDTO(Quiz q) {
        return DTOs.QuizListDTO.builder()
                .id(q.getId()).titre(q.getTitre())
                .matiere(q.getMatiere().getNom())
                .classe(q.getClasse().getNom())
                .typeQuiz(q.getTypeQuiz().name())
                .noteSur(q.getNoteSur())
                .dureeMinutes(q.getDureeMinutes())
                .dateOuverture(q.getDateOuverture())
                .dateCloture(q.getDateCloture())
                .nbQuestions(q.getQuizQuestions() != null ? q.getQuizQuestions().size() : 0)
                .estCloture(q.isEstCloture())
                .build();
    }

    private DTOs.QuizDetailDTO toDetailDTO(Quiz q, User user) {
        List<DTOs.QuestionPublicDTO> questions = q.getQuizQuestions() == null
                ? List.of()
                : q.getQuizQuestions().stream().map(qq -> {
            Question question = qq.getQuestion();
            // ENF-1 : ChoixPublicDTO sans estCorrect
            List<DTOs.ChoixPublicDTO> choix = question.getChoix() == null ? List.of() : question.getChoix().stream()
                    .map(c -> DTOs.ChoixPublicDTO.builder().id(c.getId()).texte(c.getTexte()).build())
                    .collect(Collectors.toList());
            return DTOs.QuestionPublicDTO.builder()
                    .id(question.getId()).enonce(question.getEnonce())
                    .type(question.getType().name())
                    .reponseMultiple(question.isReponseMultiple())
                    .points(qq.getPointsEffectifs()).choix(choix).build();
        }).collect(Collectors.toList());

        var builder = DTOs.QuizDetailDTO.builder()
                .id(q.getId()).titre(q.getTitre())
                .matiere(q.getMatiere().getNom())
                .typeQuiz(q.getTypeQuiz().name())
                .noteSur(q.getNoteSur())
                .dureeMinutes(q.getDureeMinutes())
                .tentativesMax(q.getTentativesMax())
                .dateCloture(q.getDateCloture())
                .estCloture(q.isEstCloture())
                .questions(questions);

        // v3.2 : infos de reprise / tentatives, utiles uniquement à l'étudiant
        if (user != null && user.isEtudiant()) {
            List<Tentative> mesTentatives = tentativeRepo.findByQuizAndEtudiant(q, user);
            mesTentatives.stream()
                    .filter(t -> t.getDateSoumission() == null)
                    .findFirst()
                    .ifPresent(t -> builder.tentativeEnCoursId(t.getId()));

            long soumises = mesTentatives.stream().filter(t -> t.getDateSoumission() != null).count();
            builder.tentativesUtilisees((int) soumises);

            if (q.getTypeQuiz() == Quiz.TypeQuiz.ENTRAINEMENT && q.getDelaiEntreTentativesMinutes() > 0) {
                tentativeRepo.findTopByQuizAndEtudiantOrderByDateDebutDesc(q, user).ifPresent(derniere -> {
                    LocalDateTime prochaine = derniere.getDateDebut().plusMinutes(q.getDelaiEntreTentativesMinutes());
                    if (LocalDateTime.now().isBefore(prochaine)) builder.prochaineTentativeDisponibleA(prochaine);
                });
            }
        }

        return builder.build();
    }

    private Quiz findQuizOrThrow(Long id) {
        return quizRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));
    }
}
