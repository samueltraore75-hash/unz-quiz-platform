package com.unz.eval.repository;

import com.unz.eval.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByLibelle(String libelle);
    Optional<Tag> findByLibelleIgnoreCase(String libelle);
}
