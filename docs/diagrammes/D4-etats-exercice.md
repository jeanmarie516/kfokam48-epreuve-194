# D4 — États-transitions du cycle de vie d'un exercice (bonus)

> Un exercice naît au dépôt et meurt relu — ou reste en attente si le relecteur ne rend jamais (Q11). Les transitions sont déclenchées par les règles RG6, RG8, RG11 et la décision D2.

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE : POST /api/exercices (dépôt, lien valide)

    EN_ATTENTE --> EN_ATTENTE : PUT /api/exercices/{id}/lien<br/>(remplacement du lien, RG8 — personne n'a commencé à relire)
    EN_ATTENTE --> EN_ATTENTE : tirage du relecteur (au dépôt ou à chaque nouvelle présence, D2/H2 — RG7)

    EN_ATTENTE --> RELU : POST ou PUT /api/relectures/{id}<br/>(note entière 0–20 validée, RG9)

    EN_ATTENTE --> [*] : session clôturée sans relecture (RG11 : reste compté dans relecturesEnAttente)
    RELU --> [*] : session clôturée (note figée définitivement — RG10/D1)

    note right of EN_ATTENTE
        Visible "en attente" dans le tableau (Q11)
        Remplacement du lien refusé (409 LIEN_VERROUILLE)
        dès que la relecture est rendue
    end note

    note right of RELU
        L'étudiant relu voit note + commentaire,
        jamais le nom du relecteur (RG14)
    end note
```

**Transitions refusées (et codes associés) :**

| Transition demandée | Condition de refus | Code HTTP / erreur |
|---|---|---|
| Remplacer le lien | Une relecture a été rendue (RG8) | 409 `LIEN_VERROUILLE` |
| Rendre une 2ᵉ relecture | Déjà rendue (RG10/D1) | 409 `RELECTURE_DEJA_RENDUE` |
| Rendre une relecture par le déposant | RG5 | 403 `AUTO_RELECTURE_INTERDITE` |
| Modifier après clôture | RG15 | 409 `SESSION_CLOTUREE` |
| Déposer après clôture | RG12/RG15 | 409 `SESSION_CLOTUREE` |
