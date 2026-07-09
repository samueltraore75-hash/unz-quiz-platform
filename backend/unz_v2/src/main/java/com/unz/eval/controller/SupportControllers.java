package com.unz.eval.controller;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.exception.ResourceNotFoundException;
import com.unz.eval.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Petits endpoints de support pour les fronts Enseignant / Admin — listes de
 * référence nécessaires aux formulaires (mes matières, classes, étudiants d'une classe).
 */

@RestController
@RequestMapping("/api/matieres")
class MyMatieresController {

    private final MatiereRepository matiereRepo;

    public MyMatieresController(MatiereRepository matiereRepo) {
        this.matiereRepo = matiereRepo;
    }

    /** Matières enseignées par l'utilisateur connecté (toutes années confondues) */
    @GetMapping("/mine")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<List<DTOs.MatiereDTO>> mine(@AuthenticationPrincipal User enseignant) {
        List<Matiere> matieres = matiereRepo.findByAffectations_Enseignant(enseignant);
        return ResponseEntity.ok(matieres.stream().map(m -> DTOs.MatiereDTO.builder()
                .id(m.getId()).nom(m.getNom()).coefficient(m.getCoefficient())
                .ueNom(m.getUe().getNom()).build()).collect(Collectors.toList()));
    }
}

@RestController
@RequestMapping("/api/classes")
class ClassesReadController {

    private final ClasseRepository classeRepo;
    private final UserRepository userRepo;
    private final QuizRepository quizRepo;

    public ClassesReadController(ClasseRepository classeRepo, UserRepository userRepo, QuizRepository quizRepo) {
        this.classeRepo = classeRepo;
        this.userRepo = userRepo;
        this.quizRepo = quizRepo;
    }

    /** Liste des classes — lecture seule, utile pour peupler les formulaires (métadonnées, pas de données personnelles).
     *  Volontairement public (pas de @PreAuthorize) : utilisé par le formulaire d'inscription, avant connexion. */
    @GetMapping
    public ResponseEntity<List<DTOs.ClasseDTO>> list() {
        return ResponseEntity.ok(classeRepo.findAll().stream().map(c -> DTOs.ClasseDTO.builder()
                .id(c.getId()).nom(c.getNom())
                .niveauLibelle(c.getNiveau().getLibelle())
                .anneeLibelle(c.getAnneeAcademique().getLibelle()).build()).collect(Collectors.toList()));
    }

    /**
     * Étudiants inscrits dans une classe (pour la saisie de notes, par ex.).
     * ENF-2 (v3.1) : un enseignant ne peut consulter les noms des étudiants que
     * pour une classe où il a effectivement un devoir — jamais le trombinoscope
     * complet d'une classe qu'il n'enseigne pas. L'Administrateur n'a pas cette
     * restriction.
     */
    @GetMapping("/{id}/etudiants")
    @PreAuthorize("hasAnyRole('ENSEIGNANT','ADMIN')")
    public ResponseEntity<List<DTOs.UserDTO>> etudiants(@PathVariable Long id, @AuthenticationPrincipal User demandeur) {
        Classe classe = classeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Classe introuvable"));
        if (demandeur.isEnseignant() && !quizRepo.existsByClasseAndCreePar(classe, demandeur))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez consulter que les étudiants d'une classe pour laquelle vous avez créé un devoir.");
        List<DTOs.UserDTO> etudiants = userRepo.findByInscriptions_Classe(classe).stream()
                .map(u -> DTOs.UserDTO.builder()
                        .id(u.getId()).username(u.getUsername())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .role(u.getRole().name()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(etudiants);
    }
}
