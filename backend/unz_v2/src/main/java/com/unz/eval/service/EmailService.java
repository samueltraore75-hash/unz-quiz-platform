package com.unz.eval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envoi des e-mails transactionnels de la plateforme (validation de compte,
 * mot de passe temporaire, lien de réinitialisation). Volontairement défensif :
 * un problème d'envoi ne doit JAMAIS faire échouer l'action admin qui l'a
 * déclenché — on logge simplement l'erreur.
 *
 * v5 : envoi via l'API HTTP de Brevo (https://api.brevo.com) au lieu de SMTP
 * direct. Motif : les hébergeurs gratuits (Render, Railway...) bloquent le
 * trafic sortant vers les ports SMTP (25/465/587) pour limiter le spam — seul
 * le HTTPS (port 443) reste ouvert. Passer par une API HTTP contourne cette
 * restriction sans dépendre du plan payant d'un hébergeur.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    @Value("${app.mail.from:}")
    private String from;

    @Value("${app.mail.from-name:UNZ Quiz}")
    private String fromName;

    @Value("${app.mail.api-key:}")
    private String apiKey;

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

    // ── v3.6 : notifications par e-mail, en complément des notifications in-app ──

    public void envoyerQuizDisponible(String destinataire, String prenom, String quizTitre, String lien) {
        String sujet = "Nouveau devoir disponible : " + quizTitre;
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Le devoir \"" + quizTitre + "\" est maintenant disponible sur UNZ Quiz.\n"
                + "Vous pouvez le consulter ici : " + lien + "\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    public void envoyerNotesDisponibles(String destinataire, String prenom, String quizTitre, String lien) {
        String sujet = "Résultats disponibles : " + quizTitre;
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Vos résultats pour \"" + quizTitre + "\" sont maintenant disponibles.\n"
                + "Consultez-les ici : " + lien + "\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    public void envoyerBulletinDisponible(String destinataire, String prenom, String semestreLabel, String lien) {
        String sujet = "Votre bulletin du semestre " + semestreLabel + " est disponible";
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Votre bulletin du semestre " + semestreLabel + " vient d'être publié.\n"
                + "Consultez-le ici : " + lien + "\n\n"
                + "— L'équipe UNZ Quiz";
        envoyer(destinataire, sujet, corps);
    }

    public void envoyerNoteSaisie(String destinataire, String prenom, String matiere, String valeur, String lien) {
        String sujet = "Nouvelle note en " + matiere;
        String corps = "Bonjour " + safe(prenom) + ",\n\n"
                + "Une note de " + valeur + "/20 vient d'être enregistrée en " + matiere + ".\n"
                + "Consultez le détail ici : " + lien + "\n\n"
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
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[email] app.mail.api-key non configurée — envoi annulé (sujet=\"{}\")", sujet);
            return;
        }
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", fromName, "email", from),
                    "to", List.of(Map.of("email", destinataire)),
                    "subject", sujet,
                    "textContent", corps
            );
            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("[email] envoyé à={} sujet=\"{}\"", destinataire, sujet);
            } else {
                log.error("[email] échec d'envoi à={} sujet=\"{}\" : HTTP {} — {}",
                        destinataire, sujet, response.statusCode(), response.body());
            }
        } catch (Exception e) {
            // On avale volontairement l'erreur : un souci d'envoi ne doit jamais
            // empêcher l'admin de valider un compte ou réinitialiser un mot de passe.
            log.error("[email] échec d'envoi à={} sujet=\"{}\" : {}", destinataire, sujet, e.getMessage());
        }
    }

    private String safe(String v) { return v == null || v.isBlank() ? "" : v; }
}
