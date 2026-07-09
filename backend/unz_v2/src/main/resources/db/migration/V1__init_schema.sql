-- ============================================================================
-- UNZ Eval Platform v3 — schéma de base complet
-- Convention : snake_case, InnoDB, utf8mb4, clés étrangères explicites.
-- Organisé en 6 domaines, dans l'ordre de dépendance (parents avant enfants).
-- ============================================================================

SET NAMES utf8mb4;

-- ============================================================================
-- DOMAINE 1 — ORGANISATION ACADÉMIQUE
-- ============================================================================

CREATE TABLE filieres (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom         VARCHAR(150) NOT NULL,
    code        VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_filieres_nom  UNIQUE (nom),
    CONSTRAINT uq_filieres_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE niveaux (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle     VARCHAR(20) NOT NULL,
    filiere_id  BIGINT NOT NULL,
    CONSTRAINT fk_niveaux_filiere FOREIGN KEY (filiere_id) REFERENCES filieres(id),
    CONSTRAINT uq_niveaux_libelle_filiere UNIQUE (libelle, filiere_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : remplace le champ texte "annee_academique" dupliqué partout.
CREATE TABLE annees_academiques (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle      VARCHAR(20) NOT NULL,              -- ex : "2025-2026"
    date_debut   DATE NOT NULL,
    date_fin     DATE NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_annees_libelle UNIQUE (libelle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE classes (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom                  VARCHAR(100) NOT NULL,
    niveau_id            BIGINT NOT NULL,
    annee_academique_id  BIGINT NOT NULL,
    CONSTRAINT fk_classes_niveau FOREIGN KEY (niveau_id) REFERENCES niveaux(id),
    CONSTRAINT fk_classes_annee  FOREIGN KEY (annee_academique_id) REFERENCES annees_academiques(id),
    CONSTRAINT uq_classes_nom_annee UNIQUE (nom, annee_academique_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE semestres (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero               TINYINT NOT NULL,
    niveau_id            BIGINT NOT NULL,
    annee_academique_id  BIGINT NOT NULL,
    CONSTRAINT fk_semestres_niveau FOREIGN KEY (niveau_id) REFERENCES niveaux(id),
    CONSTRAINT fk_semestres_annee  FOREIGN KEY (annee_academique_id) REFERENCES annees_academiques(id),
    CONSTRAINT uq_semestres_numero_niveau_annee UNIQUE (numero, niveau_id, annee_academique_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ues (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom               VARCHAR(150) NOT NULL,
    credits           INT NOT NULL,
    seuil_validation  DECIMAL(4,2) NOT NULL DEFAULT 10.00,
    semestre_id       BIGINT NOT NULL,
    CONSTRAINT fk_ues_semestre FOREIGN KEY (semestre_id) REFERENCES semestres(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE matieres (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom           VARCHAR(150) NOT NULL,
    coefficient   DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    ue_id         BIGINT NOT NULL,
    CONSTRAINT fk_matieres_ue FOREIGN KEY (ue_id) REFERENCES ues(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- DOMAINE 2 — UTILISATEURS & SÉCURITÉ
-- ============================================================================

CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    -- Colonnes élargies : elles stockent du texte chiffré AES-256-GCM (base64), plus long qu'en clair.
    first_name  VARCHAR(255),
    last_name   VARCHAR(255),
    email       VARCHAR(255),
    role        VARCHAR(20) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN','ENSEIGNANT','ETUDIANT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : rattachement annuel étudiant ↔ classe. Permet la progression L1→L2→L3 sans
-- perdre l'historique (le champ classe_id fixe sur users est supprimé).
CREATE TABLE inscriptions (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id          BIGINT NOT NULL,
    classe_id            BIGINT NOT NULL,
    annee_academique_id  BIGINT NOT NULL,
    statut               VARCHAR(20) NOT NULL DEFAULT 'EN_COURS',
    date_inscription     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inscriptions_etudiant FOREIGN KEY (etudiant_id) REFERENCES users(id),
    CONSTRAINT fk_inscriptions_classe   FOREIGN KEY (classe_id) REFERENCES classes(id),
    CONSTRAINT fk_inscriptions_annee    FOREIGN KEY (annee_academique_id) REFERENCES annees_academiques(id),
    CONSTRAINT uq_inscriptions_etudiant_annee UNIQUE (etudiant_id, annee_academique_id),
    CONSTRAINT chk_inscriptions_statut CHECK (statut IN ('EN_COURS','REDOUBLANT','ABANDON','DIPLOME'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : remplace le lien simple matieres.enseignant_id, permet co-intervention et historique.
CREATE TABLE affectations_enseignants (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    enseignant_id        BIGINT NOT NULL,
    matiere_id           BIGINT NOT NULL,
    annee_academique_id  BIGINT NOT NULL,
    CONSTRAINT fk_affectations_enseignant FOREIGN KEY (enseignant_id) REFERENCES users(id),
    CONSTRAINT fk_affectations_matiere    FOREIGN KEY (matiere_id) REFERENCES matieres(id),
    CONSTRAINT fk_affectations_annee      FOREIGN KEY (annee_academique_id) REFERENCES annees_academiques(id),
    CONSTRAINT uq_affectations UNIQUE (enseignant_id, matiere_id, annee_academique_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : mot de passe oublié.
CREATE TABLE password_reset_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pwd_reset_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_pwd_reset_token UNIQUE (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    action         VARCHAR(100) NOT NULL,
    description    VARCHAR(500),
    username       VARCHAR(50)  NOT NULL,
    role           VARCHAR(20),
    ip_address     VARCHAR(45),
    status         VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message  VARCHAR(500),
    timestamp      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_audit_status CHECK (status IN ('SUCCESS','FAILURE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_audit_username  ON audit_logs(username);
CREATE INDEX idx_audit_action    ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

CREATE TABLE notifications (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinataire_id BIGINT NOT NULL,
    type           VARCHAR(30) NOT NULL,
    titre          VARCHAR(150) NOT NULL,
    message        TEXT NOT NULL,
    lue            BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id   BIGINT,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_destinataire FOREIGN KEY (destinataire_id) REFERENCES users(id),
    CONSTRAINT chk_notifications_type CHECK (type IN
        ('QUIZ_OUVERT','QUIZ_CLOTURE','NOTES_DISPONIBLES','BULLETIN_PUBLIE','NOTE_SAISIE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX idx_notifications_destinataire ON notifications(destinataire_id, lue);

-- ============================================================================
-- DOMAINE 3 — BANQUE DE CONTENU PÉDAGOGIQUE
-- ============================================================================

CREATE TABLE questions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    enonce            TEXT NOT NULL,
    type              VARCHAR(20) NOT NULL DEFAULT 'QCM_UNIQUE',
    difficulte        VARCHAR(15) NOT NULL DEFAULT 'MOYEN',
    points            DOUBLE NOT NULL DEFAULT 1.0,
    matiere_id        BIGINT NOT NULL,
    CONSTRAINT fk_questions_matiere FOREIGN KEY (matiere_id) REFERENCES matieres(id),
    CONSTRAINT chk_questions_type CHECK (type IN ('QCM_UNIQUE','QCM_MULTIPLE','VRAI_FAUX','REPONSE_COURTE')),
    CONSTRAINT chk_questions_difficulte CHECK (difficulte IN ('FACILE','MOYEN','DIFFICILE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE choix (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    texte         VARCHAR(500) NOT NULL,
    est_correct   BOOLEAN NOT NULL DEFAULT FALSE,
    question_id   BIGINT NOT NULL,
    CONSTRAINT fk_choix_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : thématiques / chapitres pour filtrer la banque de questions.
CREATE TABLE tags (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    libelle  VARCHAR(80) NOT NULL,
    CONSTRAINT uq_tags_libelle UNIQUE (libelle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE question_tags (
    question_id  BIGINT NOT NULL,
    tag_id       BIGINT NOT NULL,
    PRIMARY KEY (question_id, tag_id),
    CONSTRAINT fk_qtags_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_qtags_tag      FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : image/PDF illustrant une question.
CREATE TABLE pieces_jointes (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    url           VARCHAR(500) NOT NULL,
    type_mime     VARCHAR(50) NOT NULL,
    question_id   BIGINT NOT NULL,
    CONSTRAINT fk_pj_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- DOMAINE 4 — ÉVALUATION
-- ============================================================================

CREATE TABLE quizzes (
    id                                BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre                             VARCHAR(200) NOT NULL,
    type_quiz                         VARCHAR(20) NOT NULL DEFAULT 'EXAMEN',
    duree_minutes                     INT NOT NULL DEFAULT 30,
    tentatives_max                    INT NOT NULL DEFAULT 1,
    delai_entre_tentatives_minutes    INT NOT NULL DEFAULT 0,
    date_ouverture                    DATETIME,
    date_cloture                      DATETIME,
    cloture_validee_par_enseignant    BOOLEAN NOT NULL DEFAULT FALSE,
    matiere_id                        BIGINT NOT NULL,
    classe_id                         BIGINT NOT NULL,
    cree_par_id                       BIGINT NOT NULL,
    CONSTRAINT fk_quizzes_matiere  FOREIGN KEY (matiere_id) REFERENCES matieres(id),
    CONSTRAINT fk_quizzes_classe   FOREIGN KEY (classe_id) REFERENCES classes(id),
    CONSTRAINT fk_quizzes_cree_par FOREIGN KEY (cree_par_id) REFERENCES users(id),
    CONSTRAINT chk_quizzes_type CHECK (type_quiz IN ('EXAMEN','ENTRAINEMENT'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE quiz_questions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id           BIGINT NOT NULL,
    question_id       BIGINT NOT NULL,
    ordre             INT NOT NULL DEFAULT 0,
    points_override   DOUBLE,
    CONSTRAINT fk_qq_quiz     FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT fk_qq_question FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT uq_qq_quiz_question UNIQUE (quiz_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tentatives (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id           BIGINT NOT NULL,
    etudiant_id       BIGINT NOT NULL,
    date_debut        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_soumission   DATETIME,
    note_obtenue      DECIMAL(6,2),
    feedback_enseignant TEXT,
    CONSTRAINT fk_tentatives_quiz     FOREIGN KEY (quiz_id) REFERENCES quizzes(id),
    CONSTRAINT fk_tentatives_etudiant FOREIGN KEY (etudiant_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reponses (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tentative_id   BIGINT NOT NULL,
    question_id    BIGINT NOT NULL,
    reponse_texte  VARCHAR(500),
    CONSTRAINT fk_reponses_tentative FOREIGN KEY (tentative_id) REFERENCES tentatives(id) ON DELETE CASCADE,
    CONSTRAINT fk_reponses_question  FOREIGN KEY (question_id) REFERENCES questions(id),
    CONSTRAINT uq_reponses_tentative_question UNIQUE (tentative_id, question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reponse_choix (
    reponse_id  BIGINT NOT NULL,
    choix_id    BIGINT NOT NULL,
    PRIMARY KEY (reponse_id, choix_id),
    CONSTRAINT fk_rc_reponse FOREIGN KEY (reponse_id) REFERENCES reponses(id) ON DELETE CASCADE,
    CONSTRAINT fk_rc_choix   FOREIGN KEY (choix_id) REFERENCES choix(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : anti-triche — trace les événements suspects pendant une tentative.
CREATE TABLE tentative_evenements (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tentative_id   BIGINT NOT NULL,
    type           VARCHAR(30) NOT NULL,
    horodatage     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_te_tentative FOREIGN KEY (tentative_id) REFERENCES tentatives(id) ON DELETE CASCADE,
    CONSTRAINT chk_te_type CHECK (type IN ('PERTE_FOCUS','COPIER_COLLER','CHANGEMENT_ONGLET','RETOUR_FOCUS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- DOMAINE 5 — RÉSULTATS ACADÉMIQUES
-- ============================================================================

CREATE TABLE notes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id     BIGINT NOT NULL,
    matiere_id      BIGINT NOT NULL,
    type_note       VARCHAR(20) NOT NULL,
    session_examen  VARCHAR(15) NOT NULL DEFAULT 'NORMALE',
    valeur          DECIMAL(4,2) NOT NULL,
    ponderation     DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    saisie_par_id   BIGINT,
    date_saisie     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notes_etudiant   FOREIGN KEY (etudiant_id) REFERENCES users(id),
    CONSTRAINT fk_notes_matiere    FOREIGN KEY (matiere_id) REFERENCES matieres(id),
    CONSTRAINT fk_notes_saisie_par FOREIGN KEY (saisie_par_id) REFERENCES users(id),
    CONSTRAINT chk_notes_type CHECK (type_note IN ('QUIZ','EXAMEN_ECRIT','TP','CONTROLE_CONTINU')),
    CONSTRAINT chk_notes_session CHECK (session_examen IN ('NORMALE','RATTRAPAGE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bulletins (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id         BIGINT NOT NULL,
    semestre_id         BIGINT NOT NULL,
    session_examen      VARCHAR(15) NOT NULL DEFAULT 'NORMALE',
    moyenne_generale    DECIMAL(4,2),
    credits_acquis      INT NOT NULL DEFAULT 0,
    credits_total       INT NOT NULL DEFAULT 0,
    semestre_valide     BOOLEAN NOT NULL DEFAULT FALSE,
    publie              BOOLEAN NOT NULL DEFAULT FALSE,
    date_publication    DATETIME,
    genere_par_id       BIGINT,
    CONSTRAINT fk_bulletins_etudiant   FOREIGN KEY (etudiant_id) REFERENCES users(id),
    CONSTRAINT fk_bulletins_semestre   FOREIGN KEY (semestre_id) REFERENCES semestres(id),
    CONSTRAINT fk_bulletins_genere_par FOREIGN KEY (genere_par_id) REFERENCES users(id),
    CONSTRAINT uq_bulletins_etudiant_semestre_session UNIQUE (etudiant_id, semestre_id, session_examen),
    CONSTRAINT chk_bulletins_session CHECK (session_examen IN ('NORMALE','RATTRAPAGE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- NOUVEAU : délibération de jury avant publication définitive.
CREATE TABLE deliberations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    bulletin_id     BIGINT NOT NULL,
    decision        VARCHAR(20) NOT NULL,
    commentaire     TEXT,
    valide_par_id   BIGINT NOT NULL,
    date_deliberation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_deliberations_bulletin   FOREIGN KEY (bulletin_id) REFERENCES bulletins(id),
    CONSTRAINT fk_deliberations_valide_par FOREIGN KEY (valide_par_id) REFERENCES users(id),
    CONSTRAINT uq_deliberations_bulletin UNIQUE (bulletin_id),
    CONSTRAINT chk_deliberations_decision CHECK (decision IN ('VALIDE','AJOURNE','EXCLU'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================================
-- DOMAINE 6 — SUIVI & TRAÇABILITÉ
-- ============================================================================

-- NOUVEAU : trace persistante de chaque import CSV (au lieu d'un résultat jetable).
CREATE TABLE import_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    matiere_id      BIGINT NOT NULL,
    importe_par_id  BIGINT NOT NULL,
    nb_importees    INT NOT NULL DEFAULT 0,
    nb_erreurs      INT NOT NULL DEFAULT 0,
    date_import     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_logs_matiere      FOREIGN KEY (matiere_id) REFERENCES matieres(id),
    CONSTRAINT fk_import_logs_importe_par  FOREIGN KEY (importe_par_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
