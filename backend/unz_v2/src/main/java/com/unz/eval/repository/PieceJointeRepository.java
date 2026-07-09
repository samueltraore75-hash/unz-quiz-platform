package com.unz.eval.repository;

import com.unz.eval.entity.PieceJointe;
import com.unz.eval.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Long> {
    List<PieceJointe> findByQuestion(Question question);
}
