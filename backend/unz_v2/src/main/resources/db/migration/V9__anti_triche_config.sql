-- ============================================================================
-- V9 — Configuration anti-triche par devoir
-- ============================================================================
-- v3.3 : chaque enseignant peut désormais régler, par devoir :
--   - seuil_alerte_score       : score de suspicion (pondéré) à partir duquel
--                                le client déclenche une soumission automatique
--   - plein_ecran_obligatoire  : force le mode plein écran pendant la tentative
--                                (pertinent uniquement pour les EXAMEN)
ALTER TABLE quizzes
    ADD COLUMN seuil_alerte_score      INT NOT NULL DEFAULT 15,
    ADD COLUMN plein_ecran_obligatoire BOOLEAN NOT NULL DEFAULT FALSE;

-- La V1 limitait tentative_evenements.type à 4 valeurs — on y ajoute SORTIE_PLEIN_ECRAN.
ALTER TABLE tentative_evenements DROP CHECK chk_te_type;

ALTER TABLE tentative_evenements ADD CONSTRAINT chk_te_type CHECK (type IN
    ('PERTE_FOCUS','COPIER_COLLER','CHANGEMENT_ONGLET','RETOUR_FOCUS','SORTIE_PLEIN_ECRAN'));
