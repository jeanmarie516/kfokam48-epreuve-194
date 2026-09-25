-- V1 — Schéma initial (cohérent avec docs/diagrammes/D2-modele-donnees.md)
CREATE TABLE promotion (
    id   BIGSERIAL PRIMARY KEY,
    nom  VARCHAR(120) NOT NULL
);

CREATE TABLE etudiant (
    id           BIGSERIAL PRIMARY KEY,
    promotion_id BIGINT       NOT NULL REFERENCES promotion (id),
    nom          VARCHAR(120) NOT NULL
);
CREATE INDEX idx_etudiant_promotion ON etudiant (promotion_id);

CREATE TABLE session (
    id            BIGSERIAL PRIMARY KEY,
    promotion_id  BIGINT       NOT NULL REFERENCES promotion (id),
    titre         VARCHAR(200) NOT NULL,
    code          VARCHAR(6)   NOT NULL UNIQUE,
    ouverture_at  TIMESTAMP    NOT NULL,
    expiration_at TIMESTAMP    NOT NULL,
    cloturee      BOOLEAN      NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_session_promotion ON session (promotion_id);

-- RG2 : unicité d'une présence par (session, étudiant)
CREATE TABLE presence (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT    NOT NULL REFERENCES session (id),
    etudiant_id BIGINT    NOT NULL REFERENCES etudiant (id),
    source      VARCHAR(12) NOT NULL CHECK (source IN ('ETUDIANT', 'FORMATEUR')),
    creee_at    TIMESTAMP NOT NULL,
    CONSTRAINT uq_presence UNIQUE (session_id, etudiant_id)
);
CREATE INDEX idx_presence_session ON presence (session_id);

-- Contrat : 409 exercice déjà déposé → unicité (session, étudiant)
CREATE TABLE exercice (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT    NOT NULL REFERENCES session (id),
    etudiant_id BIGINT    NOT NULL REFERENCES etudiant (id),
    lien        VARCHAR(500) NOT NULL,
    statut      VARCHAR(12)  NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE', 'RELU')),
    depose_at   TIMESTAMP    NOT NULL,
    CONSTRAINT uq_exercice UNIQUE (session_id, etudiant_id)
);
CREATE INDEX idx_exercice_session ON exercice (session_id);

-- RG6 : un seul relecteur par exercice → unicité sur exercice_id
CREATE TABLE relecture (
    id          BIGSERIAL PRIMARY KEY,
    exercice_id BIGINT      NOT NULL UNIQUE REFERENCES exercice (id),
    relecteur_id BIGINT     NOT NULL REFERENCES etudiant (id),
    note        INTEGER     CHECK (note BETWEEN 0 AND 20),
    commentaire VARCHAR(2000),
    rendue      BOOLEAN     NOT NULL DEFAULT FALSE,
    rendue_at   TIMESTAMP,
    CONSTRAINT uq_relecteur_exercice UNIQUE (exercice_id, relecteur_id)
);
CREATE INDEX idx_relecture_relecteur ON relecture (relecteur_id);
