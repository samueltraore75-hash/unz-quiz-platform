package com.unz.eval.repository;

import com.unz.eval.entity.AnneeAcademique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnneeAcademiqueRepository extends JpaRepository<AnneeAcademique, Long> {
    Optional<AnneeAcademique> findByActiveTrue();
    Optional<AnneeAcademique> findByLibelle(String libelle);
}
