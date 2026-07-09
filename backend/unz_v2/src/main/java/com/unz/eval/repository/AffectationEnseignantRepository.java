package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AffectationEnseignantRepository extends JpaRepository<AffectationEnseignant, Long> {
    List<AffectationEnseignant> findByEnseignant(User enseignant);
    List<AffectationEnseignant> findByMatiere(Matiere matiere);
    List<AffectationEnseignant> findByMatiereAndAnneeAcademique(Matiere matiere, AnneeAcademique annee);
    boolean existsByEnseignantAndMatiere(User enseignant, Matiere matiere);

    // Suppression définitive d'un utilisateur : vérifie s'il a des affectations en tant qu'enseignant
    boolean existsByEnseignant(User enseignant);
}
