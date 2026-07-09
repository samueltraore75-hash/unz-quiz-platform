-- ============================================================================
-- V8 — Ajout du type DEMANDE_COMPTE à la contrainte CHECK sur notifications.type
-- ============================================================================
-- La V1 avait posé une contrainte CHECK limitant notifications.type à une liste
-- fixe de valeurs. Cette liste ne prévoyait pas les notifications envoyées aux
-- admins lors d'une nouvelle demande d'inscription (DEMANDE_COMPTE, ajouté côté
-- Java dans Notification.Type), ce qui faisait échouer l'insertion en base
-- avec "Check constraint 'chk_notifications_type' is violated."
ALTER TABLE notifications DROP CHECK chk_notifications_type;

ALTER TABLE notifications ADD CONSTRAINT chk_notifications_type CHECK (type IN
    ('QUIZ_OUVERT','QUIZ_CLOTURE','NOTES_DISPONIBLES','BULLETIN_PUBLIE','NOTE_SAISIE','DEMANDE_COMPTE'));
