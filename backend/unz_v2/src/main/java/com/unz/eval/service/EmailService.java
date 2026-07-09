package com.unz.eval.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi des e-mails transactionnels de la plateforme (validation de compte,
 * mot de passe temporaire). Volontairement défensif : un problème d'envoi
 * (SMTP mal configuré, Gmail indisponible...) ne doit JAMAIS faire échouer
 * l'action admin qui a déclenché l'e-mail — on logge simplement l'erreur.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.from-name:UNZ Quiz}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void envoyerBienvenue(String destinataire, String prenom, String username) {
        String sujet = "Votre compte UNZ Quiz a été validé";
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Bonne nouvelle : votre compte UNZ Quiz vient d'être validé par l'administrateur.\n"
                + "Vous pouvez dès à présent vous connecter avec votre identifiant : " + username + "\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    public void envoyerMotDePasseTemporaire(String destinataire, String prenom, String username, String motDePasseTemporaire) {
        String sujet = "Votre mot de passe UNZ Quiz a été réinitialisé";
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "L'administrateur vient de réinitialiser votre mot de passe UNZ Quiz.\n"
                + "Identifiant : " + username + "\n"
                + "Mot de passe temporaire : " + motDePasseTemporaire + "\n\n"
                + "Merci de vous connecter et de le changer dès que possible depuis votre profil.\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    /**
     * Mot de passe oublié en self-service (v4) : au lieu d'envoyer un mot de passe
     * en clair par e-mail, on envoie un lien à usage unique et à durée de vie
     * limitée vers la page de réinitialisation du frontend.
     */
    public void envoyerLienReinitialisation(String destinataire, String prenom, String lien) {
        String sujet = "Réinitialisation de votre mot de passe UNZ Quiz";
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Vous avez demandé la réinitialisation de votre mot de passe UNZ Quiz.\n"
                + "Cliquez sur le lien ci-dessous pour choisir un nouveau mot de passe (valable 30 minutes) :\n"
                + lien + "\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez simplement cet e-mail : "
                + "votre mot de passe actuel reste inchangé.\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    private void envoyer(String destinataire, String sujet, String corps) {
        if (!enabled) {
            log.info("[email désactivé] à={} sujet=\"{}\" — envoi sauté (app.mail.enabled=false)", destinataire, sujet);
            return;
        }
        if (destinataire == null || destinataire.isBlank()) {
            log.warn("[email] destinataire manquant, envoi annulé (sujet=\"{}\")", sujet);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(destinataire);
            message.setSubject(sujet);
            message.setText(corps);
            mailSender.send(message);
            log.info("[email] envoyé à={} sujet=\"{}\"", destinataire, sujet);
        } catch (Exception e) {
            // On avale volontairement l'erreur : un souci SMTP ne doit jamais
            // empêcher l'admin de valider un compte ou réinitialiser un mot de passe.
            log.error("[email] échec d'envoi à={} sujet=\"{}\" : {}", destinataire, sujet, e.getMessage());
        }
    }

    private String safe(String v) { return v == null || v.isBlank() ? "" : v; }
}
