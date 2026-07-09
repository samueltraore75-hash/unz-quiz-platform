package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    List<Classe> findByNiveau(Niveau niveau);
    List<Classe> findByNiveau_Filiere(Filiere filiere);
    List<Classe> findByAnneeAcademique(AnneeAcademique anneeAcademique);
}
