package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // EF-7 : banque de questions par matière
    List<Question> findByMatiere(Matiere matiere);
    // v3 : passe par AffectationEnseignant (remplace l'ancien Matiere.enseignant direct)
    List<Question> findByMatiereAndMatiere_Affectations_Enseignant(Matiere matiere, User enseignant);
}
