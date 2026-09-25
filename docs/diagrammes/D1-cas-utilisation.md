# D1 — Diagramme de cas d'utilisation

> Acteurs : **Formateur** et **Étudiant**. Le relecteur est un étudiant portant ce rôle contextuel (cf. §2 du cahier des charges) — il apparaît comme spécialisation de l'étudiant. Aucune authentification (Q1).

```mermaid
graph LR
    F((Formateur))
    E((Étudiant))
    R((Relecteur))

    R -. est un .- E

    subgraph Application K48
        UC1([Ouvrir une session et obtenir un code])
        UC2([Voir le code et son expiration])
        UC3([Ajouter une présence à la main])
        UC4([Clôturer la session])
        UC5([Consulter le tableau par étudiant])
        UC6([Marquer sa présence avec le code])
        UC7([Déposer le lien de son exercice])
        UC8([Remplacer son lien avant relecture])
        UC9([Voir sa note et son commentaire])
        UC10([Rendre une relecture notée])
        UC11([Corriger sa relecture avant clôture])
        UC12([Voir les exercices à relire])
    end

    F --> UC1
    F --> UC2
    F --> UC3
    F --> UC4
    F --> UC5
    E --> UC6
    E --> UC7
    E --> UC8
    E --> UC9
    R --> UC10
    R --> UC11
    R --> UC12

    UC3 -. inclut .-> UC2
    UC1 -. inclut .-> UC2
```

**Références :** EF1–EF16 · RG1 (expiration), RG3 (présence avant clôture), RG4 (source FORMATEUR), RG5 (jamais son propre exercice), RG8 (lien verrouillé après début de relecture), RG10 (correction avant clôture), RG14 (anonymat du relecteur).
