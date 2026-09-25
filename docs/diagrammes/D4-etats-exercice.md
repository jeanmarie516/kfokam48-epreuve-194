# D4 — États-transitions du cycle de vie d'un exercice (bonus)

> **Version 2 — enveloppe étape 3 :** deux relecteurs par exercice (RG6 v2). L'exercice passe à `RELU` dès la **première** relecture rendue (sa note s'affiche alors **provisoire**, RG17) ; la note devient définitive (moyenne des deux, RG16) quand les **deux** sont rendues. Si aucune n'est rendue, l'exercice reste en attente (RG18).

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE : POST /api/exercices (dépôt, lien valide)

    EN_ATTENTE --> EN_ATTENTE : PUT /api/exercices/{id}/lien<br/>(remplacement du lien, RG8 — personne n'a commencé à relire)
    EN_ATTENTE --> EN_ATTENTE : tirage du relecteur (au dépôt ou à chaque nouvelle présence, D2/H2 — RG7)

    EN_ATTENTE --> RELU : première relecture rendue<br/>(note affichée PROVISOIRE — RG17)
    RELU --> RELU : deuxième relecture rendue<br/>(note définitive = moyenne des deux — RG16, D3)

    EN_ATTENTE --> [*] : session clôturée sans relecture rendue (RG18 : reste compté dans relecturesEnAttente des 2 relecteurs)
    RELU --> [*] : session clôturée (note figée définitivement — RG10/D1)

    note right of EN_ATTENTE
        Visible "en attente" dans le tableau (Q11/RG18)
        Sortie du périmètre maintenu : remplacement du lien (S1)
    end note

    note right of RELU
        L'étudiant relu voit note + commentaire(s),
        jamais le nom des relecteurs (RG14)
        provisoire=true tant que la 2e relecture
        n'est pas rendue (RG17)
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
