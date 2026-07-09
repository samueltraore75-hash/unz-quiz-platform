package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {
    List<Matiere> findByUe(UE ue);

    /** v3 : passe par AffectationEnseignant (remplace l'ancien findByEnseignant direct) */
    List<Matiere> findByAffectations_Enseignant(User enseignant);
}
