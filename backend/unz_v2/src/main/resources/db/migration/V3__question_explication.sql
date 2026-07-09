-- ============================================================================
-- v3.2 — explication pédagogique optionnelle sur une question, affichée à
-- l'étudiant après correction (nouvelle fonctionnalité UX).
-- ============================================================================

ALTER TABLE questions
    ADD COLUMN explication TEXT NULL AFTER enonce;
