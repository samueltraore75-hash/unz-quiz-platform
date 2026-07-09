package com.unz.eval.repository;

import com.unz.eval.entity.Niveau;
import com.unz.eval.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NiveauRepository extends JpaRepository<Niveau, Long> {
    List<Niveau> findByFiliere(Filiere filiere);
    Optional<Niveau> findByLibelleAndFiliere(String libelle, Filiere filiere);
}