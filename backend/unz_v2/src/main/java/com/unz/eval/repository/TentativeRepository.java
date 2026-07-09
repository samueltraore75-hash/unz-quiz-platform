package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TentativeRepository extends JpaRepository<Tentative, Long> {
    // ENF-2 : toujours filtré sur l'étudiant connecté
    List<Tentative> findByEtudiantOrderByDateDebutDesc(User etudiant);
    List<Tentative> findByQuizAndEtudiant(Quiz quiz, User etudiant);
    List<Tentative> findByQuizAndDateSoumissionIsNotNull(Quiz quiz);

    // EF-11 : progression par matière
    @Query("SELECT t FROM Tentative t WHERE t.etudiant = :etudiant " +
           "AND t.quiz.matiere = :matiere AND t.dateSoumission IS NOT NULL " +
           "ORDER BY t.dateSoumission ASC")
    List<Tentative> findProgressionParMatiere(
            @Param("etudiant") User etudiant,
            @Param("matiere") Matiere matiere);

    // Stats anonymisées (EF-14) : count par quiz
    long countByQuizAndDateSoumissionIsNotNull(Quiz quiz);

    // EF-16 : dernière tentative d'un étudiant pour un quiz
    Optional<Tentative> findTopByQuizAndEtudiantOrderByDateDebutDesc(Quiz quiz, User etudiant);

    boolean existsByQuiz(Quiz quiz);

    // Suppression définitive d'un utilisateur : vérifie s'il a des tentatives
    boolean existsByEtudiant(User etudiant);
}
