-- ============================================================================
-- v3.2 — données d'amorçage indispensables au premier lancement.
-- Sans ce fichier, impossible de se connecter (aucun compte n'existe, et créer
-- un utilisateur nécessite déjà d'être ADMIN — un vrai blocage au démarrage).
--
-- Identifiants du compte administrateur initial :
--   utilisateur : admin
--   mot de passe : Admin123!
-- Le hash ci-dessous est un BCrypt (coût 12, identique à SecurityConfig).
-- CHANGEZ CE MOT DE PASSE dès la première connexion (aucun mécanisme
-- "changer mon mot de passe" n'existe encore côté profil — la ré-initialisation
-- passe par un autre compte admin, donc changez-le en base ou ajoutez cet
-- écran avant un vrai déploiement).
-- ============================================================================

-- IMPORTANT : first_name / last_name / email sont chiffrés par AesEncryptor au
-- niveau JPA (ENF-6). Une insertion SQL brute ne passe pas par ce convertisseur :
-- les laisser NULL ici évite un échec de déchiffrement au premier chargement.
-- Utilisez l'API (PUT /api/profil ou équivalent, à venir) pour les renseigner
-- une fois connecté — elles seront alors chiffrées correctement.
INSERT INTO users (username, password, first_name, last_name, email, role, active)
VALUES (
    'admin',
    '$2b$12$lUEcKpnNwsGzJZoC2TWD0eVgDC9U6wOQ55jBHphC1Ao5dNsjmVMvm',
    NULL, NULL, NULL, 'ADMIN', TRUE
);

-- Année académique active par défaut — nécessaire pour inscrire le premier étudiant.
INSERT INTO annees_academiques (libelle, date_debut, date_fin, active)
VALUES ('2025-2026', '2025-10-01', '2026-07-31', TRUE);
