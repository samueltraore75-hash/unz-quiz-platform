package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    // EF-19 : toutes les notes d'un étudiant pour une matière
    List<Note> findByEtudiantAndMatiere(User etudiant, Matiere matiere);
    List<Note> findByMatiere(Matiere matiere);

    // ENF-2 : notes d'un étudiant uniquement
    List<Note> findByEtudiant(User etudiant);

    // Suppression définitive d'un utilisateur : vérifie s'il a des notes (comme étudiant ou comme correcteur)
    boolean existsByEtudiant(User etudiant);
    boolean existsBySaisieParr(User saisieParr);
}
