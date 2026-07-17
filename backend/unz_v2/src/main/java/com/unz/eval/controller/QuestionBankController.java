package com.unz.eval.controller;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD complet de la banque de questions (EF-7), en complément de l'import CSV en masse.
 * Jusqu'ici seul l'import CSV existait — impossible de créer/éditer une question à la main.
 */
@RestController
@RequestMapping("/api/questions")
@PreAuthorize("hasRole('ENSEIGNANT')")
class QuestionBankController {

    private final QuestionRepository questionRepo;
    private final MatiereRepository matiereRepo;
    private final QuizQuestionRepository qqRepo;
    private final TagRepository tagRepo;

    public QuestionBankController(QuestionRepository questionRepo, MatiereRepository matiereRepo,
                                   QuizQuestionRepository qqRepo, TagRepository tagRepo) {
        this.questionRepo = questionRepo;
        this.matiereRepo = matiereRepo;
        this.qqRepo = qqRepo;
        this.tagRepo = tagRepo;
    }

    /**
     * Banque de questions d'une matière que l'enseignant connecté enseigne.
     * v3.4 : filtres facultatifs — difficulte (FACILE/MOYEN/DIFFICILE), tag (libellé exact),
     * recherche (sous-chaîne de l'énoncé, insensible à la casse). Le volume par matière reste
     * modeste, donc le filtrage se fait simplement en mémoire après le chargement.
     */
    @GetMapping
    public ResponseEntity<List<DTOs.QuestionFullDTO>> list(@RequestParam Long matiereId,
                                                             @RequestParam(required = false) String difficulte,
                                                             @RequestParam(required = false) String tag,
                                                             @RequestParam(required = false) String recherche,
                                                             @AuthenticationPrincipal User enseignant) {
        Matiere matiere = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        verifierProprietaire(matiere, enseignant);

        String rechercheNormalisee = recherche == null ? null : recherche.trim().toLowerCase();
        return ResponseEntity.ok(questionRepo.findByMatiere(matiere).stream()
                .filter(q -> difficulte == null || q.getDifficulte().name().equalsIgnoreCase(difficulte))
                .filter(q -> tag == null || (q.getTags() != null
                        && q.getTags().stream().anyMatch(t -> t.getLibelle().equalsIgnoreCase(tag))))
                .filter(q -> rechercheNormalisee == null
                        || q.getEnonce().toLowerCase().contains(rechercheNormalisee))
                .map(this::toDTO).collect(Collectors.toList()));
    }

    /** Liste des tags existants sur la banque d'une matière, pour peupler le filtre côté client */
    @GetMapping("/tags")
    public ResponseEntity<List<String>> tags(@RequestParam Long matiereId, @AuthenticationPrincipal User enseignant) {
        Matiere matiere = matiereRepo.findById(matiereId)
                .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        verifierProprietaire(matiere, enseignant);
        return ResponseEntity.ok(questionRepo.findByMatiere(matiere).stream()
                .flatMap(q -> q.getTags() == null ? java.util.stream.Stream.<Tag>empty() : q.getTags().stream())
                .map(Tag::getLibelle).distinct().sorted().collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DTOs.QuestionFullDTO> detail(@PathVariable Long id,
                                                         @AuthenticationPrincipal User enseignant) {
        Question q = findOrThrow(id);
        verifierProprietaire(q.getMatiere(), enseignant);
        return ResponseEntity.ok(toDTO(q));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<DTOs.QuestionFullDTO> create(@Valid @RequestBody DTOs.SaveQuestionRequest req,
                                                         @AuthenticationPrincipal User enseignant) {
        Matiere matiere = matiereRepo.findById(req.getMatiereId())
                .orElseThrow(() -> new ResourceNotFoundException("Matière introuvable"));
        verifierProprietaire(matiere, enseignant);
        validerType(req);

        Question q = Question.builder()
                .enonce(req.getEnonce())
                .explication(req.getExplication())
                .type(Question.TypeQuestion.valueOf(req.getType()))
                .difficulte(Question.Difficulte.valueOf(req.getDifficulte()))
                .points(req.getPoints())
                .matiere(matiere)
                .build();
        q.setChoix(construireChoix(req, q));
        q.setTags(construireTags(req.getTags()));
        questionRepo.save(q); // cascade ALL : les choix sont persistés avec la question
        return ResponseEntity.status(201).body(toDTO(q));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<DTOs.QuestionFullDTO> update(@PathVariable Long id,
                                                         @Valid @RequestBody DTOs.SaveQuestionRequest req,
                                                         @AuthenticationPrincipal User enseignant) {
        Question q = findOrThrow(id);
        verifierProprietaire(q.getMatiere(), enseignant);
        validerType(req);

        q.setEnonce(req.getEnonce());
        q.setExplication(req.getExplication());
        q.setType(Question.TypeQuestion.valueOf(req.getType()));
        q.setDifficulte(Question.Difficulte.valueOf(req.getDifficulte()));
        q.setPoints(req.getPoints());
        // orphanRemoval=true sur Question.choix : remplacer la liste supprime les anciens choix
        q.setChoix(construireChoix(req, q));
        q.setTags(construireTags(req.getTags()));
        questionRepo.save(q);
        return ResponseEntity.ok(toDTO(q));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User enseignant) {
        Question q = findOrThrow(id);
        verifierProprietaire(q.getMatiere(), enseignant);
        if (qqRepo.existsByQuestion(q))
            throw new BadRequestException(
                    "Cette question est utilisée dans un ou plusieurs quiz et ne peut pas être supprimée.");
        questionRepo.delete(q);
        return ResponseEntity.ok().build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void validerType(DTOs.SaveQuestionRequest req) {
        Question.TypeQuestion type = Question.TypeQuestion.valueOf(req.getType());
        boolean besoinDeChoix = type == Question.TypeQuestion.QCM_UNIQUE
                || type == Question.TypeQuestion.QCM_MULTIPLE
                || type == Question.TypeQuestion.VRAI_FAUX;
        if (besoinDeChoix && (req.getChoix() == null || req.getChoix().isEmpty()))
            throw new BadRequestException("Ce type de question nécessite au moins un choix de réponse.");
        if (besoinDeChoix && req.getChoix().stream().noneMatch(DTOs.CreateChoixRequest::isEstCorrect))
            throw new BadRequestException("Au moins un choix doit être marqué comme correct.");
    }

    private List<Choix> construireChoix(DTOs.SaveQuestionRequest req, Question q) {
        if (req.getChoix() == null) return List.of();
        return req.getChoix().stream()
                .map(c -> Choix.builder().texte(c.getTexte()).estCorrect(c.isEstCorrect()).question(q).build())
                .collect(Collectors.toList());
    }

    /** Retrouve chaque tag par libellé (insensible à la casse) ou le crée s'il n'existe pas encore */
    private List<Tag> construireTags(List<String> libelles) {
        if (libelles == null) return List.of();
        return libelles.stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .distinct()
                .map(l -> tagRepo.findByLibelleIgnoreCase(l).orElseGet(() -> {
                    Tag t = new Tag();
                    t.setLibelle(l);
                    return tagRepo.save(t);
                }))
                .collect(Collectors.toList());
    }

    private void verifierProprietaire(Matiere matiere, User enseignant) {
        if (!matiere.estEnseignePar(enseignant))
            throw new AccessDeniedException("Vous ne pouvez gérer que les questions de vos matières.");
    }

    private Question findOrThrow(Long id) {
        return questionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Question introuvable"));
    }

    private DTOs.QuestionFullDTO toDTO(Question q) {
        List<DTOs.ChoixCorrigeDTO> choix = q.getChoix() == null ? List.of() : q.getChoix().stream()
                .map(c -> DTOs.ChoixCorrigeDTO.builder()
                        .id(c.getId()).texte(c.getTexte()).estCorrect(c.isEstCorrect()).build())
                .collect(Collectors.toList());
        List<String> tags = q.getTags() == null ? List.of() : q.getTags().stream()
                .map(Tag::getLibelle).sorted().collect(Collectors.toList());
        return DTOs.QuestionFullDTO.builder()
                .id(q.getId()).enonce(q.getEnonce()).explication(q.getExplication())
                .type(q.getType().name()).difficulte(q.getDifficulte().name())
                .points(q.getPoints()).matiereNom(q.getMatiere().getNom())
                .choix(choix).tags(tags).build();
    }
}
