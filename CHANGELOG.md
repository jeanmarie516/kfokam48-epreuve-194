# CHANGELOG

Tout ce qui est notable dans ce projet est documenté ici.
Le format s'inspire de [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/) ; le versionning suit [SemVer](https://semver.org/lang/fr/).

## [0.1.0] — Première version

### Ajouté

- **Sessions** — le formateur ouvre une session et obtient un code de présence unique,
  expirant 15 minutes après l'ouverture (EF2, RG1). Clôture explicite (EF11, RG15)
  et consultation du code (complément du contrat). [PR #15, #16]
- **Présences** — l'étudiant marque sa présence avec le code : 201 / 400 code inconnu /
  409 déjà présent / 410 code expiré / 429 bloqué après 5 erreurs (RG13, Q4) ;
  ajout manuel par le formateur tracé `source=FORMATEUR` (EF12, RG4, Q14). [PR #17]
- **Tirage du relecteur** — au dépôt de l'exercice, retenté à chaque nouvelle présence
  (décision D2/H2), au hasard parmi les présents, jamais le déposant (EF7, RG5, RG6, RG7). [PR #17]
- **Exercices** — dépôt d'un lien par session (409 si doublon, 400 lien invalide),
  possible jusqu'à la clôture même après expiration du code (EF6, EF16, RG12) ;
  remplacement du lien verrouillé après relecture (EF10, RG8). [PR #18]
- **Relectures** — note entière 0–20 + commentaire ; soumission unique (POST),
  correction possible jusqu'à la clôture (PUT) — contradiction Q10/Q15 tranchée
  en D1 (EF8, EF9, RG9, RG10, RG15). [PR #19]
- **Tableau du formateur** — par étudiant : présence à chaque session avec source,
  exercices déposés, moyenne des notes reçues (API, jamais recalculée côté client),
  relectures en attente (EF13, EF15, Q16). Référentiels des écrans et vue étudiant
  avec relecture anonymisée (RG14). [PR #20]
- **Frontend React** — trois écrans (formateur, étudiant, relecteur), couche API
  dédiée, états de chargement et d'erreur, moyenne affichée telle que renvoyée
  par l'API (F1, F2, F3). [PR #22]
- **Tests** — 4 tests unitaires sur les règles métier réelles (RG5, RG9, D1, RG10)
  et 4 tests d'intégration sur `POST /api/presences` et `GET /api/tableau`, sur H2
  en mémoire (B6). [PR #21]
- **Analyse** — cahier des charges (16 EF, 15 RG), diagrammes D1–D4 en Mermaid,
  contrat d'API complété, backlog de 14 issues, journal de bord. [PR avant tout code]
- **Démarrage** — `docker compose up` ou 3 commandes documentées ; données de
  démonstration chargées au démarrage.

### Corrigé

- RG5 : un déposant tentant de relire son propre exercice recevait 404 au lieu du
  403 attendu par le contrat — la règle est maintenant vérifiée en premier. [PR #21]

### Non livré (repriorisé)

- **Ticket #14 (Could)** — Docker Compose : reporté, l'option « 3 commandes » du
  sujet est honorée. [voir ci-dessous si livré en v1.0]

## [1.0.0] — Version finale

### Ajouté

- **Docker Compose** (ticket #14) — `docker compose up` démarre PostgreSQL, le backend
  et le frontend ; données de démonstration incluses.
- **README d'installation** testé depuis un clone vierge.

### Repriorisation écrite

- L'enveloppe de l'étape 3 n'était pas encore remise à la livraison v1.0 ; traitée
  ensuite en v1.1.0 (voir ci-dessous), le jalon final étant reposé après.

## [1.1.0] — Enveloppe étape 3 : bug + changement de besoin

### Corrigé (bug #24)

- **Course sur les présences** — deux soumissions quasi simultanées du même étudiant :
  la perdante recevait 409 CONFLIT générique au lieu du 409 DEJA_PRESENT du contrat.
  Preuve dans l'historique : test d'intégration concurrent committé rouge, correctif,
  même test vert. [PR fix/course-presences]

### Ajouté (changement de besoin — issues #25, #26, #27)

- **Deux relecteurs distincts par exercice** (RG6 v2 remplace Q6) : tirage jusqu'à
  2 relecteurs parmi les présents, complété à chaque nouvelle présence (D2/H2).
- **Note retenue** : moyenne des deux relectures rendues, au dixième près sans arrondi
  (RG16, décision D3) ; provisoire si une seule rendue (RG17) ; en attente si aucune (RG18).
- **Migration V3** ajoutée (V1/V2 jamais modifiées) : unicité (exercice, relecteur) —
  les données déjà en base survivent. [PR feature/deux-relecteurs]
- **Contrat v1.1.0** : NoteRetenue { valeur, provisoire, commentaires } ; moyenne du
  tableau portant sur les notes retenues.
- **Analyse mise à jour** (cahier des charges v2, D2/D4 corrigés) dans un commit dédié
  qui le dit.

### Retiré (sacrifice assumé S1)

- **EF10/RG8 (remplacement du lien)** sort du périmètre maintenu : avec deux relecteurs,
  remplacer un lien après le premier rendu crée une incohérence entre relectures.
  L'endpoint reste conforme au contrat ; le formulaire disparaît de l'écran étudiant.
