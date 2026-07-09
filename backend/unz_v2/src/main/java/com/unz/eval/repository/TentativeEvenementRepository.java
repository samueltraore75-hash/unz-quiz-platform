package com.unz.eval.repository;

import com.unz.eval.entity.Tentative;
import com.unz.eval.entity.TentativeEvenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TentativeEvenementRepository extends JpaRepository<TentativeEvenement, Long> {
    List<TentativeEvenement> findByTentative(Tentative tentative);
    long countByTentativeAndType(Tentative tentative, TentativeEvenement.Type type);
}
