package com.unz.eval.config;

import com.unz.eval.entity.User;
import com.unz.eval.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nettoyage automatique au démarrage (idempotent — sans effet si déjà propre) :
 *   1. Calcule email_hash pour les comptes créés avant l'introduction de ce champ
 *      (les nouveaux comptes l'ont déjà, via User#computeEmailHash).
 *   2. Détecte les comptes qui partagent le même e-mail (aucune contrainte
 *      d'unicité n'existait avant la V7) : le compte le plus ancien (plus
 *      petit id) garde son e-mail, les autres se le voient retiré — sans quoi
 *      la contrainte UNIQUE ajoutée en V7 empêcherait l'application de démarrer.
 *      Un WARNING est loggé pour chaque compte concerné, à corriger à la main
 *      dans l'admin (Utilisateurs → renseigner un e-mail correct).
 */
@Component
public class EmailHashBackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EmailHashBackfillRunner.class);

    private final UserRepository userRepo;

    public EmailHashBackfillRunner(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<User> tous = userRepo.findAllByOrderByIdAsc();
        Map<String, User> dejaVus = new HashMap<>();
        int doublons = 0;

        for (User u : tous) {
            if (u.getEmail() == null || u.getEmail().isBlank()) continue;
            String hash = User.hashEmail(u.getEmail());

            User premier = dejaVus.get(hash);
            if (premier == null) {
                dejaVus.put(hash, u);
                if (!hash.equals(u.getEmailHash())) userRepo.save(u); // rattrape les anciens comptes sans hash
            } else {
                log.warn("[email_hash] doublon détecté : le compte '{}' (id={}) et '{}' (id={}) partagent le même e-mail. "
                        + "L'e-mail est conservé sur '{}' (le plus ancien) et retiré de '{}' — "
                        + "pensez à corriger son e-mail dans le panneau admin.",
                        premier.getUsername(), premier.getId(), u.getUsername(), u.getId(),
                        premier.getUsername(), u.getUsername());
                u.setEmail(null); // @PreUpdate remet aussi email_hash à null automatiquement
                userRepo.save(u);
                doublons++;
            }
        }

        if (doublons > 0) {
            log.warn("[email_hash] {} compte(s) ont eu leur e-mail retiré à cause d'un doublon — à corriger manuellement.", doublons);
        }
    }
}
