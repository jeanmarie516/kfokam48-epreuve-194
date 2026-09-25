# D2 — Modèle de données

> Ce diagramme doit rester **cohérent avec les migrations Flyway** du backend (`V1__schema_initial.sql`). Une colonne ajoutée ici doit exister dans la migration, et réciproquement.

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION : "organise"
    SESSION ||--o{ PRESENCE : "concerne"
    ETUDIANT ||--o{ PRESENCE : "possede"
    SESSION ||--o{ EXERCICE : "recueille"
    ETUDIANT ||--o{ EXERCICE : "depose"
    EXERCICE ||--o| RELECTURE : "fait l objet de"
    ETUDIANT ||--o{ RELECTURE : "assigne comme relecteur"

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
- `RELECTURE` : une seule par exercice (RG6) — unicité sur `exercice_id` ; `note` entier 0–20 (RG9) ; `rendue=false` = relecture en attente (RG11) ; le relecteur est un étudiant présent à la session et jamais le déposant (RG5, RG7).
- L'anonymat du relecteur (RG14) est garanti par l'API : le DTO renvoyé à l'étudiant relu n'expose jamais `relecteur_id` ni le nom du relecteur.
