-- ============================================================================
-- v3.1 — barème déclaré du devoir (noté sur 20, 40, 50...) + rattrapage du
-- lien manquant entre clôture d'un devoir Examen et création de la note
-- officielle (EF-18). Ce fichier ne modifie que le schéma ; le rattachement
-- Note se fait côté application (QuizService.cloturerQuiz).
-- ============================================================================

ALTER TABLE quizzes
    ADD COLUMN note_sur DECIMAL(6,2) NOT NULL DEFAULT 20.00 AFTER type_quiz;
