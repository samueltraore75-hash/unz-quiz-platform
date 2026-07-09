package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReponseRepository extends JpaRepository<Reponse, Long> {
    List<Reponse> findByTentative(Tentative tentative);
    Optional<Reponse> findByTentativeAndQuestion(Tentative tentative, Question question);

    // EF-14 : stats anonymisées — combien de fois un choix a été sélectionné
    @Query("SELECT COUNT(r) FROM Reponse r JOIN r.choixSelectionnes c " +
           "WHERE r.tentative.quiz = :quiz AND c.id = :choixId " +
           "AND r.tentative.dateSoumission IS NOT NULL")
    long countSelectionsChoix(@Param("quiz") Quiz quiz, @Param("choixId") Long choixId);
}
