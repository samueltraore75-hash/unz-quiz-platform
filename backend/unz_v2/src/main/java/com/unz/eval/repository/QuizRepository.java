package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // EF-2 : l'étudiant ne voit que les quiz de sa classe
    List<Quiz> findByClasse(Classe classe);
    // Enseignant ne voit que ses quiz
    List<Quiz> findByCreePar(User creePar);
    boolean existsByClasseAndCreePar(Classe classe, User creePar);

    // Suppression définitive d'un utilisateur : vérifie s'il a créé des quiz
    boolean existsByCreePar(User creePar);
}
