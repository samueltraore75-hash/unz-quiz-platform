package com.unz.eval.service;

import com.unz.eval.dto.DTOs;
import com.unz.eval.entity.*;
import com.unz.eval.repository.*;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Statistiques globales pour le tableau de bord Admin.
 * Vue d'ensemble par acteur (classes, enseignants, matières) — distincte des
 * statistiques par quiz (StatsService), qui restent réservées aux enseignants.
 */
@Service
public class AdminStatsService {

    private final UserRepository userRepo;
    private final ClasseRepository classeRepo;
    private final MatiereRepository matiereRepo;
    private final NoteRepository noteRepo;
    private final QuizRepository quizRepo;

    public AdminStatsService(UserRepository userRepo, ClasseRepository classeRepo,
                              MatiereRepository matiereRepo, NoteRepository noteRepo,
                              QuizRepository quizRepo) {
        this.userRepo = userRepo;
        this.classeRepo = classeRepo;
        this.matiereRepo = matiereRepo;
        this.noteRepo = noteRepo;
        this.quizRepo = quizRepo;
    }

    public DTOs.AdminStatsDTO getStats() {
        List<User> etudiants = userRepo.findByRole(User.Role.ETUDIANT);
        List<User> enseignants = userRepo.findByRole(User.Role.ENSEIGNANT);
        List<Classe> classes = classeRepo.findAll();
        List<Quiz> quizzes = quizRepo.findAll();
        List<Note> notes = noteRepo.findAll();

        List<DTOs.EffectifClasseDTO> effectifs = classes.stream()
                .map(c -> DTOs.EffectifClasseDTO.builder()
                        .classeNom(c.getNom())
                        .nbEtudiants(userRepo.findByInscriptions_Classe(c).size())
                        .build())
                .sorted(Comparator.comparingLong(DTOs.EffectifClasseDTO::getNbEtudiants).reversed())
                .collect(Collectors.toList());

        List<DTOs.MoyenneMatiereDTO> moyennes = matiereRepo.findAll().stream()
                .map(m -> {
                    List<Note> notesMatiere = noteRepo.findByMatiere(m);
                    double moyenne = notesMatiere.isEmpty() ? 0.0 : notesMatiere.stream()
                            .mapToDouble(n -> n.getValeur().doubleValue())
                            .average().orElse(0.0);
                    return DTOs.MoyenneMatiereDTO.builder()
                            .matiereNom(m.getNom())
                            .moyenne(Math.round(moyenne * 100.0) / 100.0)
                            .nbNotes(notesMatiere.size())
                            .build();
                })
                .filter(dto -> dto.getNbNotes() > 0)
                .sorted(Comparator.comparingDouble(DTOs.MoyenneMatiereDTO::getMoyenne).reversed())
                .collect(Collectors.toList());

        List<DTOs.ActiviteEnseignantDTO> activite = enseignants.stream()
                .map(e -> DTOs.ActiviteEnseignantDTO.builder()
                        .enseignantNom((e.getFirstName() != null ? e.getFirstName() : "") + " " +
                                (e.getLastName() != null ? e.getLastName() : e.getUsername()))
                        .nbDevoirs(quizRepo.findByCreePar(e).size())
                        .build())
                .sorted(Comparator.comparingLong(DTOs.ActiviteEnseignantDTO::getNbDevoirs).reversed())
                .collect(Collectors.toList());

        double moyenneGenerale = notes.isEmpty() ? 0.0 : notes.stream()
                .mapToDouble(n -> n.getValeur().doubleValue())
                .average().orElse(0.0);

        return DTOs.AdminStatsDTO.builder()
                .nbEtudiants(etudiants.size())
                .nbEnseignants(enseignants.size())
                .nbClasses(classes.size())
                .nbDevoirs(quizzes.size())
                .moyenneGenerale(Math.round(moyenneGenerale * 100.0) / 100.0)
                .effectifsParClasse(effectifs)
                .moyennesParMatiere(moyennes)
                .activiteEnseignants(activite)
                .build();
    }
}
