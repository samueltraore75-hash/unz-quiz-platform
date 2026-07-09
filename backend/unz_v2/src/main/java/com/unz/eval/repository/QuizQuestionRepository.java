package com.unz.eval.repository;

import com.unz.eval.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByQuizOrderByOrdreAsc(Quiz quiz);
    boolean existsByQuizAndQuestion(Quiz quiz, Question question);
    boolean existsByQuestion(Question question);
    int countByQuiz(Quiz quiz);
}
