# D3 — Séquence : marquer sa présence

> Cas nominal et cas d'erreur, en cohérence stricte avec le contrat d'API : `400 CODE_INCONNU`, `409 DEJA_PRESENT`, `410 CODE_EXPIRE`, `429 ETUDIANT_BLOQUE` (RG13). Format d'erreur imposé : `{ code, message }`.

```mermaid
sequenceDiagram
    autonumber
    participant E as Étudiant
    participant F as Front (apiClient)
    participant API as PresenceController
    participant S as PresenceService
    participant DB as PresenceRepository

    E->>F: saisit le code de présence
    F->>API: POST /api/presences { code, etudiantId }

    API->>S: enregistrer(code, etudiantId)
    S->>S: RG13 — l'étudiant est-il bloqué<br/>(5 erreurs consécutives) ?
    alt étudiant bloqué (RG13)
        S-->>API: EtudiantBloqueException
        API-->>F: 429 { code: "ETUDIANT_BLOQUE", message }
    else code inconnu
        S-->>API: CodeInconnuException
        S->>S: incrémente le compteur d'erreurs (RG13)
        API-->>F: 400 { code: "CODE_INCONNU", message }
    else code expiré (RG1)
        S-->>API: CodeExpireException
        API-->>F: 410 { code: "CODE_EXPIRE", message }
    else session clôturée (RG3)
        S-->>API: SessionClotureeException
        API-->>F: 409 { code: "SESSION_CLOTUREE", message }
    else déjà présent (RG2)
        S-->>API: DejaPresentException
        API-->>F: 409 { code: "DEJA_PRESENT", message }
    else cas nominal
        S->>DB: insert presence (source=ETUDIANT)
        S->>S: RG7 — tente le tirage d'un relecteur<br/>pour les exercices sans relecteur (H2)
        S-->>API: Presence enregistrée
        API-->>F: 201 { id, sessionId, etudiantId, source }
    end

    F-->>E: état de chargement puis résultat<br/>(succès ou message d'erreur formaté)
```

**Notes :**

- L'ordre des vérifications est celui du service : blocage RG13 → code inconnu → expiration RG1 → clôture RG3 → unicité RG2. Un code expiré est un code **connu** : la distinction 400/410 est faite par comparaison avec `expirationAt`.
- La session clôturée refuse aussi la présence par code : Q3 (« après la fin de la session : non ») se réalise via la clôture explicite du formateur (RG3).
- En cas de succès, l'enregistrement d'une présence peut débloquer le tirage d'un relecteur pour un exercice déposé avant que quelqu'un d'autre ne soit présent (décision D2/H2 du cahier des charges).
