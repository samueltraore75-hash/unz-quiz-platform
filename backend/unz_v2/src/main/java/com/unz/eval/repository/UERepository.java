package com.unz.eval.repository;

import com.unz.eval.entity.Semestre;
import com.unz.eval.entity.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UERepository extends JpaRepository<UE, Long> {
    List<UE> findBySemestre(Semestre semestre);
}
