package com.unz.eval.service;

import com.unz.eval.entity.AnneeAcademique;
import com.unz.eval.exception.BadRequestException;
import com.unz.eval.repository.AnneeAcademiqueRepository;
import org.springframework.stereotype.Service;

/**
 * v3 : point d'accès unique à l'année académique active.
 * Utilisé partout où on avait auparavant "user.getClasse()" directement —
 * désormais la classe d'un étudiant dépend de l'année académique en cours.
 */
@Service
public class AnneeAcademiqueService {

    private final AnneeAcademiqueRepository anneeRepo;

    public AnneeAcademiqueService(AnneeAcademiqueRepository anneeRepo) {
        this.anneeRepo = anneeRepo;
    }

    public AnneeAcademique getAnneeActive() {
        return anneeRepo.findByActiveTrue()
                .orElseThrow(() -> new BadRequestException(
                        "Aucune année académique active. Contactez l'administrateur."));
    }
}
