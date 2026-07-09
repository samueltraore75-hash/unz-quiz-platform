package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SemestreRepository extends JpaRepository<Semestre, Long> {
    List<Semestre> findByNiveauAndAnneeAcademique(Niveau niveau, AnneeAcademique anneeAcademique);
    Optional<Semestre> findByNumeroAndNiveauAndAnneeAcademique(
            Integer numero, Niveau niveau, AnneeAcademique anneeAcademique);
}
