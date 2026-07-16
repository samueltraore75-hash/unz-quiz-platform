package com.unz.eval.service;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.*;
import com.unz.eval.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistiques pédagogiques anonymisées.
 * EF-14  : taux de réussite, répartition des choix, évolution entre tentatives.
 * ENF-3  : aucun identifiant individuel retourné (statistiquesQuiz).
 * ENF-4  : double protection — permissions + données anonymisées.
 * EF-15  : consultables même après clôture.
 * EF-13  : notesClasse() est un carnet de notes nominatif (légitime, distinct des stats EF-14).
 */
@Service
public class StatsService {

    private final QuizRepository quizRepo;
    private final TentativeRepository tentativeRepo;
    private final ReponseRepository reponseRepo;
    private final TentativeEvenementRepository evenementRepo;

    public StatsService(QuizRepository quizRepo, TentativeRepository tentativeRepo, ReponseRepository reponseRepo, TentativeEvenementRepository evenementRepo) {
        this.quizRepo = quizRepo;
        this.tentativeRepo = tentativeRepo;
        this.reponseRepo = reponseRepo;
        this.evenementRepo = evenementRepo;
    }

    public DTOs.QuizStatsDTO statistiquesQuiz(Long quizId, User user) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));

        if (user.isEnseignant() && !quiz.getCreePar().getId().equals(user.getId()))
            throw new AccessDeniedException("Vous ne pouvez consulter que les statistiques de vos quiz.");

        List<Tentative> tentativesValides = tentativeRepo
                .findByQuizAndDateSoumissionIsNotNull(quiz);

        List<DTOs.StatsQuestionDTO> statsParQuestion = quiz.getQuizQuestions().stream()
                .sorted(Comparator.comparingInt(QuizQuestion::getOrdre))
                .map(qq -> buildStatsQuestion(qq.getQuestion(), quiz, tentativesValides))
                .collect(Collectors.toList());

        return DTOs.QuizStatsDTO.builder()
                .quizTitre(quiz.getTitre())
                .statistiques(statsParQuestion)
                .build();
    }

    private DTOs.StatsQuestionDTO buildStatsQuestion(Question question, Quiz quiz,
                                                       List<Tentative> tentatives) {
        List<Reponse> reponses = tentatives.stream()
                .flatMap(t -> t.getReponses() != null ? t.getReponses().stream() : java.util.stream.Stream.empty())
                .filter(r -> r.getQuestion().getId().equals(question.getId()))
                .collect(Collectors.toList());

        int nbReponses = reponses.size();

        long nbCorrectes = reponses.stream().filter(Reponse::estCorrecte).count();
        double tauxReussite = nbReponses > 0 ? (double) nbCorrectes / nbReponses * 100 : 0;
        tauxReussite = Math.round(tauxReussite * 10.0) / 10.0;

        List<DTOs.StatsChoixDTO> repartition = question.getChoix().stream().map(choix -> {
            long nbSel = reponses.stream()
                    .filter(r -> r.getChoixSelectionnes() != null &&
                            r.getChoixSelectionnes().stream().anyMatch(c -> c.getId().equals(choix.getId())))
                    .count();
            double pct = nbReponses > 0 ? Math.round((double) nbSel / nbReponses * 1000.0) / 10.0 : 0;
            return DTOs.StatsChoixDTO.builder()
                    .choixId(choix.getId()).texte(choix.getTexte())
                    .estCorrect(choix.isEstCorrect())
                    .nbSelections(nbSel).pourcentage(pct)
                    .build();
        }).collect(Collectors.toList());

        Double tauxT1 = null, tauxSuivantes = null;
        if (quiz.getTypeQuiz() == Quiz.TypeQuiz.ENTRAINEMENT) {
            Map<Long, List<Tentative>> parEtudiant = tentatives.stream()
                    .collect(Collectors.groupingBy(t -> t.getEtudiant().getId()));

            Set<Long> premieresIds = parEtudiant.values().stream()
                    .map(list -> list.stream()
                            .min(Comparator.comparing(Tentative::getDateDebut))
                            .map(Tentative::getId).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<Reponse> reponsesT1 = reponses.stream()
                    .filter(r -> premieresIds.contains(r.getTentative().getId()))
                    .collect(Collectors.toList());
            List<Reponse> reponsesSuivantes = reponses.stream()
                    .filter(r -> !premieresIds.contains(r.getTentative().getId()))
                    .collect(Collectors.toList());

            if (!reponsesT1.isEmpty()) {
                long c = reponsesT1.stream().filter(Reponse::estCorrecte).count();
                tauxT1 = Math.round((double) c / reponsesT1.size() * 1000.0) / 10.0;
            }
            if (!reponsesSuivantes.isEmpty()) {
                long c = reponsesSuivantes.stream().filter(Reponse::estCorrecte).count();
                tauxSuivantes = Math.round((double) c / reponsesSuivantes.size() * 1000.0) / 10.0;
            }
        }

        return DTOs.StatsQuestionDTO.builder()
                .questionId(question.getId()).enonce(question.getEnonce())
                .points(question.getPoints()).nbReponsesTotales((long) nbReponses)
                .tauxReussite(tauxReussite).repartitionChoix(repartition)
                .tauxReussiteT1(tauxT1)
                .tauxReussiteTentativesSuivantes(tauxSuivantes)
                .build();
    }

    public Map<String, Object> notesClasse(Long quizId, User enseignant) {
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz introuvable"));
        if (!quiz.getCreePar().getId().equals(enseignant.getId()))
            throw new AccessDeniedException("Vous ne pouvez voir que les notes de vos quiz.");
        if (!quiz.isClotureValideeParEnseignant())
            throw new BadRequestException("Les notes sont disponibles uniquement après clôture.");

        List<Map<String, Object>> resultats = tentativeRepo
                .findByQuizAndDateSoumissionIsNotNull(quiz).stream()
                .map(t -> {
                    double bareme = quiz.getBaremeTotal();
                    Double n20 = bareme > 0 && t.getNoteObtenue() != null
                            ? Math.round(t.getNoteObtenue().doubleValue() / bareme * 20 * 100.0) / 100.0
                            : null;
                    // v3.3 : anti-triche — nombre d'événements suspects relevés pendant cette tentative
                    long nbEvenementsSuspects = evenementRepo.findByTentative(t).size();

                    Map<String, Object> ligne = new HashMap<>();
                    ligne.put("etudiantNom", t.getEtudiant().getFullName());
                    ligne.put("noteObtenue", t.getNoteObtenue() != null ? t.getNoteObtenue() : "—");
                    ligne.put("noteSur20", n20 != null ? n20 : "—");
                    ligne.put("nbEvenementsSuspects", nbEvenementsSuspects);
                    return ligne;
                }).collect(Collectors.toList());

        OptionalDouble moyOpt = resultats.stream()
                .filter(r -> r.get("noteSur20") instanceof Double)
                .mapToDouble(r -> (Double) r.get("noteSur20")).average();

        Map<String, Object> resultat = new HashMap<>();
        resultat.put("quizTitre", quiz.getTitre());
        resultat.put("baremeTotal", quiz.getBaremeTotal());
        resultat.put("moyenneClasse", moyOpt.isPresent() ? Math.round(moyOpt.getAsDouble() * 100.0) / 100.0 : null);
        resultat.put("resultats", resultats);
        return resultat;
    }
}