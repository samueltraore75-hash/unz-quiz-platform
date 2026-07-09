package com.unz.eval.repository;

import com.unz.eval.entity.Choix;
import com.unz.eval.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoixRepository extends JpaRepository<Choix, Long> {
    List<Choix> findByQuestion(Question question);
}
