-- V3 — Enveloppe étape 3 : chaque exercice est relu par DEUX relecteurs distincts (RG6 v2).
-- Cette migration S'AJOUTE à V1/V2 (jamais modifiées en place) et est portable
-- PostgreSQL / H2 (tests) : les DROP ... IF EXISTS et la sous-requête corrélée
-- sont acceptés par les deux moteurs.
--
-- Les données déjà en base survivent :
--   * les relectures existantes (une par exercice) restent valides : la contrainte
--     d'unicité passe de « un relecteur par exercice » (exercice_id UNIQUE) à
--     « un relecteur donné ne relit qu'une fois le même exercice »
--     (exercice_id + relecteur_id UNIQUE) — les lignes existantes restent conformes ;
--   * le plafond « au plus deux relecteurs » est garanti applicativement
--     (TirageRelecteurService.NB_RELECTEURS = 2) ; un trigger plpgsql serait
--     spécifique à PostgreSQL et casserait la portabilité des tests H2.

-- 1) L'ancienne contrainte « un seul relecteur par exercice » est retirée.
--    PostgreSQL nomme relecture_exercice_id_key la contrainte UNIQUE inline de V1 ;
--    H2 en MODE=PostgreSQL lui donne un nom généré (CONSTRAINT_n).
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS relecture_exercice_id_key;
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS CONSTRAINT_9;
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS CONSTRAINT_8;
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS CONSTRAINT_7;

-- 2) Dédoublonnage défensif avant l'index unique (ne supprime rien sur une base saine :
--    V1 interdisait déjà les doublons (exercice, relecteur) via uq_relecteur_exercice).
DELETE FROM relecture a
    WHERE a.id > (SELECT MIN(b.id) FROM relecture b
                  WHERE b.exercice_id = a.exercice_id AND b.relecteur_id = a.relecteur_id);

-- 3) Unicité (exercice, relecteur) : chaque pair ne relit qu'une fois le même exercice.
CREATE UNIQUE INDEX uq_relecture_exercice_relecteur
    ON relecture (exercice_id, relecteur_id);
