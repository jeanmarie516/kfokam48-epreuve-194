# D2 — Modèle de données

> **Version 2 — enveloppe étape 3 :** un exercice est relu par **deux relecteurs distincts** (RG6 réécrite) ; la relation `EXERCICE–RELECTURE` passe de `||--o|` (0..1) à `||--|{` (exactement 2). Ce diagramme doit rester **cohérent avec les migrations Flyway** (V1, V2, V3 — V3 ajoute la contrainte « au plus deux relecteurs » sans toucher aux migrations existantes ; les données déjà en base survivent).

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION : "organise"
    SESSION ||--o{ PRESENCE : "concerne"
    ETUDIANT ||--o{ PRESENCE : "possede"
    SESSION ||--o{ EXERCICE : "recueille"
    ETUDIANT ||--o{ EXERCICE : "depose"
    EXERCICE ||--|{ RELECTURE : "fait l objet de (exactement 2, RG6 v2)"
    ETUDIANT ||--o{ RELECTURE : "assigne comme relecteur"
    ERREUR_SAISIE_CODE ||--|| ETUDIANT : "compte les erreurs RG13"

    PROMOTION {
        long id PK
        string nom
    }
    ETUDIANT {
        long id PK
        long promotion_id FK
        string nom
    }
    SESSION {
        long id PK
        long promotion_id FK
        string titre
        string code
        timestamp ouverture_at
        timestamp expiration_at
        boolean cloturee
    }
    PRESENCE {
        long id PK
        long session_id FK
        long etudiant_id FK
        string source "ETUDIANT ou FORMATEUR"
        timestamp creee_at
    }
    EXERCICE {
        long id PK
        long session_id FK
        long etudiant_id FK
        string lien
        string statut "EN_ATTENTE ou RELU"
        timestamp depose_at
    }
    RELECTURE {
        long id PK
        long exercice_id FK
        long relecteur_id FK "etudiant present, jamais le depositaire"
        integer note "entier 0-20"
        string commentaire
        boolean rendue
        timestamp rendue_at
    }
```

**Règles portées par ce modèle :**

- `SESSION.code` unique par session, `expiration_at = ouverture_at + 15 min` (RG1).
- `PRESENCE` : unicité `(session_id, etudiant_id)` — contrainte de base (RG2) ; `source` vaut `ETUDIANT` ou `FORMATEUR` (RG4).
- `EXERCICE` : unicité `(session_id, etudiant_id)` (contrat : 409 exercice déjà déposé) ; `statut` : `EN_ATTENTE` → `RELU` (cf. D4).
- `RELECTURE` : **exactement deux par exercice** (RG6 v2 — enveloppe étape 3) — unicité sur `(exercice_id, relecteur_id)` et contrôle « au plus deux relecteurs » ; `note` entier 0–20 (RG9) ; `rendue=false` = relecture en attente (RG11/RG18) ; les relecteurs sont des étudiants présents à la session et jamais le déposant (RG5, RG7).
- L'anonymat du relecteur (RG14) est garanti par l'API : le DTO renvoyé à l'étudiant relu n'expose jamais `relecteur_id` ni le nom du relecteur.
