-- V2 — Compteur d'erreurs de saisie du code (RG13 : 5 erreurs → blocage 2 minutes)
-- Le comptage est par étudiant : quand le code est erroné, aucune session ne peut lui être rattachée.
CREATE TABLE erreur_saisie_code (
    id            BIGSERIAL PRIMARY KEY,
    etudiant_id   BIGINT    NOT NULL UNIQUE REFERENCES etudiant (id),
    nb_erreurs    INTEGER   NOT NULL DEFAULT 0,
    bloque_jusqua TIMESTAMP
);
