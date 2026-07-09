package com.unz.eval.repository;

import com.unz.eval.entity.ImportLog;
import com.unz.eval.entity.Matiere;
import com.unz.eval.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {
    List<ImportLog> findByMatiereOrderByDateImportDesc(Matiere matiere);

    // Suppression définitive d'un utilisateur : vérifie s'il a réalisé des imports
    boolean existsByImportePar(User importePar);
}
