-- ============================================================================
-- V6 — Ajout du statut de compte pour validation par l'administrateur
-- Valeurs : EN_ATTENTE (inscription libre), ACTIF (validé), REJETE (refusé)
-- ============================================================================
ALTER TABLE users
    ADD COLUMN statut_compte VARCHAR(20) NOT NULL DEFAULT 'ACTIF'
    AFTER active;

-- Les comptes existants restent ACTIF
UPDATE users SET statut_compte = 'ACTIF' WHERE statut_compte IS NULL OR statut_compte = '';
