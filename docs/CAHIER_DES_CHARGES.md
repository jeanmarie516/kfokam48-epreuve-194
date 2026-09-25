# Cahier des charges — K48 Présences & Relectures

Auteur : 194 (jeanmarie516) · **Version 2.1** — v2 : mise à jour à la suite de l'**enveloppe de l'étape 3** (bug de concurrence corrigé par #24 ; changement de besoin : **deux relecteurs par exercice**, note retenue = moyenne, provisoire si une seule relecture rendue) · v2.1 : frontend migré de React (Vite) vers **Next.js** (issue #30) · Frontend choisi : **Next.js**, parce qu'il garde la simplicité de React tout en apportant un routage intégré et un build de production vérifiable d'une seule commande.

## 1. Contexte et objectif

La direction de la formation KFOKAM48 gère aujourd'hui la présence en cours et la remise des exercices de façon manuelle : appel verbal, liens d'exercices dispersés dans une messagerie, relectures entre pairs organisées de manière informelle. Le formateur n'a aucune vue consolidée : qui était présent, qui a rendu, qui a relu quoi, quelle note a été reçue.

L'application répond à ce problème avec cinq capacités, issues de la demande brute du client :

1. le formateur ouvre une session de cours et obtient un **code de présence** à communiquer ;
2. l'étudiant saisit ce code pour **marquer sa présence** ;
3. l'étudiant **dépose le lien** de son exercice pour la session ;
4. un étudiant est **assigné à la relecture** de l'exercice d'un pair : il rend une note et un commentaire ;
5. le formateur consulte un **tableau de bord** : présence et moyenne des notes par étudiant.

L'objectif n'est pas un produit riche mais un flux fiable : présence traçable, dépôt d'exercices centralisé, relecture par les pairs équitable (anonyme pour l'étudiant relu), et un tableau qui reflète tout cela sans saisie manuelle.

## 2. Acteurs et rôles

Il n'y a **pas d'authentification** (Q1) : les acteurs sont identifiés en choisissant leur identité dans des listes. « Relecteur » n'est pas un compte séparé : c'est un **rôle contextuel** porté par un étudiant, le temps d'une relecture.

| Acteur | Ce qu'il peut faire |
|---|---|
| **Formateur** | Ouvrir une session (reçoit un code de présence) ; voir le code et son expiration ; ajouter manuellement une présence (marquée « ajouté par le formateur », Q14) ; clôturer la session ; consulter le tableau de bord (Q16) ; voir clairement les exercices en attente de relecture (Q11) |
| **Étudiant** | Choisir son nom dans la liste de la promotion ; marquer sa présence avec le code (Q2, Q3) ; déposer le lien de son exercice (Q12) ; remplacer son lien tant que personne n'a commencé à le relire (Q13) ; voir la note et le commentaire reçus, **sans le nom du relecteur** (Q8) |
| **Relecteur** (rôle contextuel d'un étudiant) | Consulter l'exercice qui lui a été assigné ; rendre une note entière sur 20 et un commentaire (Q9) ; corriger sa note tant que le formateur n'a pas clôturé la session (Q10, tranché en section 7) ; **jamais** relire son propre exercice (Q5) |
| **Système** | Générer le code de présence ; le faire expirer 15 min après l'ouverture (Q2) ; bloquer 2 minutes après 5 codes erronés du même étudiant (Q4) ; tirer le relecteur **au hasard parmi les étudiants présents à la session** (Q7) ; garantir l'unicité de la présence et du dépôt |

Le formateur n'est pas dans la liste des étudiants : son accès au tableau se fait par choix du nom formateur dans l'écran dédié (pas de mot de passe, Q1).

## 3. Périmètre

**Inclus :**

- sessions de cours avec code de présence expirant (15 min, Q2) ;
- marquage de présence avec anti-épuisement de codes (5 erreurs → blocage 2 min, Q4) ;
- ajout manuel de présence par le formateur, tracé par le champ `source` (Q14) ;
- dépôt d'un lien d'exercice par session (un seul par étudiant et par session), modifiable avant première relecture (Q12, Q13) ;
- assignation automatique d'un relecteur unique, tiré au hasard parmi les présents (Q6, Q7) ;
- relecture notée sur 20 en entiers avec commentaire (Q9) ; correction possible avant clôture (Q10, tranché) ;
- tableau formateur : présence par session, exercices déposés, moyenne des notes reçues, relectures en attente (Q16) ;
- vue étudiant relu : note + commentaire, sans nom du relecteur (Q8) ;
- données de démonstration chargées au démarrage.

**Exclu :**

- tout mot de passe, compte, JWT, session d'authentification (Q1) ;
- notification par e-mail ou messagerie ;
- dépôt de fichiers (on ne gère que des **liens**) ;
- plusieurs relecteurs par exercice, relecture croisée multiple (Q6) ;
- modification d'une note **après clôture** de la session ;
- gestion des promotions elle-même (une promotion est une simple liste d'étudiants créée au démarrage ; pas de CRUD promotion exigé) ;
- répartition manuelle des relectures par le formateur (le système tire au sort, Q7) ;
- rendu visuel soigné : aucun point pour le CSS.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | L'étudiant marque sa présence avec un code | Quand je saisis un code valide et non expiré, ma présence apparaît dans le tableau du formateur avec `source=ETUDIANT` (RG2, RG3) | Must |
| EF2 | Le formateur ouvre une session et obtient un code | Quand j'ouvre une session, je reçois un code unique et une heure d'expiration à +15 min (RG1) | Must |
| EF3 | Le code expire 15 minutes après l'ouverture | Une saisie du code après expiration renvoie l'erreur `CODE_EXPIRE` (HTTP 410) et n'enregistre rien | Must |
| EF4 | Un étudiant ne peut marquer sa présence qu'une fois | Un second envoi du même étudiant pour la même session renvoie `DEJA_PRESENT` (HTTP 409) | Must |
| EF5 | Anti-épuisement des codes : 5 erreurs → blocage 2 min | Après 5 codes erronés consécutifs du même étudiant, une 6ᵉ tentative — même correcte — renvoie `ETUDIANT_BLOQUE` (HTTP 429) pendant 2 minutes | Must |
| EF6 | L'étudiant dépose le lien de son exercice pour une session | Un lien valide déposé crée l'exercice avec `statut=EN_ATTENTE` ; un second dépôt du même étudiant pour la même session renvoie `EXERCICE_DEJA_DEPOSE` (HTTP 409) ; un lien mal formé renvoie `LIEN_INVALIDE` (HTTP 400) | Must |
| EF7 | Le système assigne un relecteur au hasard parmi les présents | Après dépôt, l'exercice a exactement un relecteur (Q6), étudiant présent à la session (Q7), différent du déposant (RG5). S'il n'y a aucun autre présent au moment du dépôt, l'exercice reste sans relecteur et un relecteur est tiré à la prochaine présence (hypothèse H2) | Must |
| EF8 | Le relecteur rend une note entière sur 20 et un commentaire | Une note non entière ou hors 0–20 renvoie `NOTE_INVALIDE` (HTTP 400) ; une relecture déjà rendue renvoie `RELECTURE_DEJA_RENDUE` (HTTP 409) ; relire son propre exercice renvoie `AUTO_RELECTURE_INTERDITE` (HTTP 403, RG5) | Must |
| EF9 | Le relecteur peut corriger sa note avant la clôture | Tant que la session n'est pas clôturée, une correction modifie note et commentaire ; après clôture, toute tentative renvoie `SESSION_CLOTUREE` (HTTP 409) — décision D1 (tranchage Q10/Q15) | Must |
| ~~EF10~~ | **(v2) Sorti du périmètre maintenu — voir S1 en section 7** : le remplacement du lien n'est plus garanti testé ni maintenu (l'endpoint reste conforme au contrat complémentaire) | — | Won't (assumé) |
| EF11 | Le formateur peut clôturer la session | Après clôture, plus aucun dépôt d'exercice ni correction de relecture n'est accepté (RG6) | Must |
| EF12 | Le formateur peut ajouter une présence à la main | La présence créée porte `source=FORMATEUR`, visible dans le tableau (RG4, Q14) | Must |
| EF13 | Le formateur voit le tableau par étudiant | Pour une promotion : par étudiant, sa présence à chaque session, son nombre d'exercices déposés, la moyenne des notes reçues (entière sur 20, arrondie au point, aucun recalcul côté frontend — F3), et les relectures qu'il doit encore faire (Q16). Une promotion inconnue renvoie 404 | Must |
| EF14 | L'étudiant relu voit sa note sans le nom du relecteur | L'étudiant voit note et commentaire de chaque exercice relu ; l'anonymat du relecteur est garanti par l'API (le nom n'est jamais renvoyé dans cette vue) | Should |
| EF15 | Les exercices non relus restent visibles comme « en attente » | Dans le tableau, un exercice sans relecture rendue est compté dans `relecturesEnAttente` du relecteur assigné (Q11) | Must |
| EF16 | Dépôt d'exercice possible après la fin du cours | Le dépôt reste possible jusqu'à la clôture de la session par le formateur, même après expiration du code (Q12 vs Q2) | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| RNF1 | Volumétrie cible : ~200 étudiants, ~10 sessions/jour, quelques milliers de présences/exercices par mois | Le schéma (index sur session_id, etudiant_id) tient ces volumes ; aucun calcul lourd côté API du tableau |
| RNF2 | Usage mobile : les étudiants saisissent le code et déposent le lien depuis leur téléphone | Écrans utilisables sur petit écran (formulaires simples, champs larges) ; rendu testé en viewport mobile |
| RNF3 | Temps de réponse : < 500 ms pour les opérations simples (présence, dépôt), < 1 s pour le tableau | Réponse par un appel unique à l'API (agrégation SQL), pas de boucles N+1 côté frontend |
| RNF4 | Fiabilité des règles : les règles RG1 à RG15 sont appliquées côté serveur, jamais côté client | Le contrat d'API renvoie les erreurs correspondantes ; les tests (B6) prouvent RG1, RG5 et le contrat |
| RNF5 | Démarrage reproductible chez un tiers | `docker compose up` ou 3 commandes documentées dans le README, données de démo chargées au démarrage, testé depuis un clone vierge |
| RNF6 | Traçabilité : aucune stack trace renvoyée au client | Toutes les erreurs passent par le gestionnaire centralisé ; format d'erreur imposé vérifié par test d'intégration |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session ; après expiration, aucune présence étudiant n'est acceptée | Q2 |
| RG2 | Un étudiant ne peut marquer sa présence qu'une seule fois par session (unicité) | Déduction Q1/Q4 (absence de doublon) + unicité imposée par le contrat (409 déjà présent) |
| RG3 | La présence par code n'est possible que tant que la session n'est pas clôturée | Q3 |
| RG4 | Une présence ajoutée manuellement par le formateur est marquée `source=FORMATEUR` ; celle saisie par l'étudiant vaut `source=ETUDIANT` | Q14 |
| RG5 | Un étudiant ne peut jamais être relecteur de son propre exercice | Q5 |
| RG6 | **(v2 — enveloppe)** Un exercice est relu par **deux relecteurs distincts** (anciennement : un seul, issue de Q6 que ce changement remplace) — tirés au sort parmi les présents, jamais le déposant ; unicité (exercice, relecteur) maintenue | Q6 remplacée par l'enveloppe étape 3 |
| RG7 | Le relecteur est choisi par le système, au hasard, parmi les étudiants présents à la session | Q7 |
| RG8 | Le lien d'un exercice peut être remplacé tant que personne n'a commencé à le relire | Q13 |
| RG9 | La note est un entier entre 0 et 20 inclus | Q9 |
| RG10 | Le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session | Q10 — tranché contre Q15, voir D1 |
| RG11 | Si le relecteur ne rend jamais sa relecture, l'exercice reste « en attente » et apparaît clairement dans le tableau | Q11 |
| RG12 | Le dépôt d'exercice est possible jusqu'à la clôture de la session, même après l'expiration du code | Q12 |
| RG13 | Après 5 codes erronés consécutifs, l'étudiant est bloqué 2 minutes | Q4 |
| RG14 | L'étudiant relu voit la note et le commentaire, mais jamais le nom du relecteur | Q8 |
| RG15 | Une session clôturée n'accepte plus ni dépôt d'exercice ni correction de relecture | Q3 + Q10 (implicitement) |
| RG16 | **(v2 — enveloppe)** La note retenue d'un exercice est la **moyenne des deux relectures rendues**, au dixième près, sans arrondi (D3) | Enveloppe étape 3 |
| RG17 | **(v2 — enveloppe)** Si une seule des deux relectures est rendue, sa note est affichée **marquée provisoire** (`provisoire=true`) | Enveloppe étape 3 |
| RG18 | **(v2 — enveloppe)** Si aucune des deux relectures n'est rendue, l'exercice reste « en attente » et apparaît dans le tableau (extension de RG11 au cas deux relecteurs) | Enveloppe étape 3 + RG11 (Q11) |

## 7. Zones d'ombre, hypothèses et contradictions

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| **D1 — Quand la note devient-elle définitive ?** | Q10 : le relecteur peut corriger tant que la session n'est pas clôturée. Q15 : la note est définitive dès qu'elle est envoyée. Les deux réponses se contredisent frontalement. | **Q10 retenu, Q15 écarté.** La note est soumise une fois (409 `RELECTURE_DEJA_RENDUE` sur nouvelle soumission), mais reste corrigeable jusqu'à la clôture (EF9, RG10). | Le contrat d'API imposé lui-même prévoit un 409 « relecture déjà rendue » sur le POST : cela suppose une soumission unique + un mécanisme de correction distinct (PUT) — sinon ce 409 n'a pas de sens. Et Q11 décrit un usage réel (des relectures tardives existent), donc le client anticipe un cycle avant clôture. Q15 exprime une intention (« plus honnête pour tout le monde ») que la clôture de session réalise mieux : la note se fige pour **tous** au même moment. |
| **D2 — À quel moment le relecteur est-il tiré ?** | Personne ne l'a demandé au client : **c'est le trou**. Ni Q7 ni Q12 ne précisent le moment du tirage. | Le tirage a lieu **au dépôt** de l'exercice, parmi les présents (hors déposant). Si aucun étudiant autre n'est encore présent, l'exercice reste sans relecteur et un relecteur est tiré à chaque nouvelle présence tant qu'il en manque un (EF7, hypothèse H2). | Si le tirage se faisait à la clôture, Q10 serait impraticable : le relecteur découvrirait son assignation après la clôture, sans fenêtre pour relire ni corriger. Tirer au dépôt donne à la relecture toute la durée de la session, ce qui est le seul sens compatible avec Q10/Q11. |
| H1 — Identité sans mot de passe | Q1 : l'étudiant choisit son nom dans une liste. | Les identifiants (promotion, session, étudiant) sont sélectionnés dans des listes servies par l'API ; aucune authentification. | Décision explicite du client, à ne pas sur-ingénierer. |
| H2 — Dépôt sans autre étudiant présent | Hypothèse (trou lié à D2) | L'exercice est créé en `EN_ATTENTE` sans relecteur ; le tirage est retenté à chaque nouvelle présence de la session. | Ne pas bloquer le dépôt pour un cas limite ; garde la cohérence avec RG7 (parmi les présents). |
| H3 — Formats du code et du lien | Aucune question | Code = 6 caractères alphanumériques uniques par session ; lien = URL http(s) valide. | Choix d'implémentation minimal, suffisant pour Q2/Q13. |
| H4 — Promotion de référence | Aucune question | Une promotion = liste fixe d'étudiants chargée en données de démo ; le tableau est interrogé par `promotionId`. | Le besoin (Q16) ne demande aucune gestion de promotions au-delà de la consultation. |
| H5 — Moyenne sans note | Hypothèse | Un étudiant sans note reçue a une moyenne `null` (affichée « — »), pas 0. | 0 serait une information fausse (il n'a pas été noté). |
| **D3 — Arrondi de la moyenne de deux notes (v2, enveloppe)** | Non demandé au client : la moyenne de deux entiers peut donner un demi-point (14 et 15 → 14,5). | La moyenne est calculée **au dixième près, sans arrondi** (`14,5` s'affiche tel quel). | Q9 impose l'entier **par relecture**, pas pour la moyenne retenue ; arrondir déformerait le jugement de deux pairs. C'est le calcul de l'API (F3), le frontend ne recalcule rien. |
| **D4 — Exercice sans aucune relecture rendue (v2, enveloppe)** | Non dit explicitement par le client pour le cas deux relecteurs. | L'exercice reste `EN_ATTENTE`, compté dans les relectures en attente des deux relecteurs assignés (RG18). | Cohérent avec Q11 qui décrit ce cas pour un relecteur ; étendu naturellement à deux. |
| **S1 — Sacrifice de périmètre (v2, enveloppe)** | Le changement « deux relecteurs » est un Must qui arrive tard : quelque chose doit sortir du périmètre. | **EF10/RG8 (remplacement du lien, Q13) sort du périmètre maintenu** : l'endpoint reste conforme au contrat mais le formulaire disparaît de l'écran étudiant et la règle n'est plus garantie testée. Avec deux relecteurs, remplacer un lien après le premier rendu crée une incohérence entre relectures. | « Un périmètre réduit et assumé vaut mieux qu'un périmètre annoncé et non tenu » (enveloppe). Écrit aussi dans le journal. |

## 8. Contraintes techniques

- **B1** Java 17+, Maven, wrapper `mvnw` commité.
- **B2** Le contrat `api/contrat.yaml` est respecté à la lettre : chemins, verbes, codes de statut, format d'erreur.
- **B3** Séparation contrôleur / service / repository ; aucune requête base dans un contrôleur ; aucune entité JPA exposée en JSON — passage par des DTO.
- **B4** Validation des entrées et gestion centralisée des erreurs (`@RestControllerAdvice`) ; aucune stack trace renvoyée.
- **B5** Schéma versionné par Flyway, migrations commitées ; `ddl-auto=update` interdit hors tests.
- **B6** Deux tests qui prouvent quelque chose : un test unitaire sur une règle métier réelle (RG5/RG9), un test d'intégration sur un endpoint — exécutables sur poste vierge.
- **F1** Next.js déclaré et justifié dans le README ; le build passe (`next build`).
- **F2** Trois écrans : formateur (ouvrir une session, voir le tableau), étudiant (marquer sa présence, déposer son exercice), relecteur (faire une relecture).
- **F3** Appels API dans une couche dédiée ; états de chargement et d'erreur gérés ; la moyenne affichée vient de l'API, jamais recalculée côté frontend.
- Démarrage : `docker compose up` ou trois commandes maximum documentées ; données de démonstration au démarrage.

## 9. Livrables

| Livrable | Emplacement |
|---|---|
| Cahier des charges (ce document) | `docs/CAHIER_DES_CHARGES.md` |
| D1 — diagramme de cas d'utilisation | `docs/diagrammes/D1-cas-utilisation.md` |
| D2 — modèle de données (cohérent avec les migrations Flyway) | `docs/diagrammes/D2-modele-donnees.md` |
| D3 — séquence « marquer sa présence » (nominal + code expiré + déjà présent) | `docs/diagrammes/D3-sequence-presence.md` |
| D4 (bonus) — états-transitions du cycle de vie d'un exercice | `docs/diagrammes/D4-etats-exercice.md` |
| Contrat d'API complété | `api/contrat.yaml` |
| Backlog en issues | Issues GitHub de ce dépôt (labels Must/Should/Could) |
| Backend | `backend/` (Spring Boot, Maven, mvnw) |
| Frontend | `frontend/` (Next.js 14, React 18) |
| Journal de bord | `docs/JOURNAL.md` |
| CHANGELOG et README d'installation | `CHANGELOG.md`, `README.md` |
| Soumission | `SOUMISSION.md` (téléversé sur la plateforme) |

## 10. Démarche prévue

Les cinq étapes du sujet corrigé (les 4 corrections communiquées après le lancement sont intégrées : terme unique « issue », commit de vérification sans préfixe `[JALON]`, **suppression de l'ancienne épreuve Git sur bundle**, enveloppe remise par le surveillant), menées dans cet ordre, avec un jalon Git poussé aux étapes 1, 2 et 4 :

1. **Analyse et conception** — ce cahier des charges, les diagrammes D1–D4, le contrat d'API complété, le backlog en issues sur ce dépôt unique. Jalon `[JALON] analyse` avant tout commit de code.
2. **V0.1** — les stories Must uniquement : une branche par issue, une PR par branche, issues fermées par les commits. Migrations Flyway dès la première issue backend (le schéma est versionné avant l'étape 3, c'est une leçon du sujet). Jalon `[JALON] v0.1`.
3. **Enveloppe** (document remis par le surveillant après le jalon v0.1) — issue ouverte avant de coder, bug reproduit, migration versionnée, contrat mis à jour, correctif et évolution séparés (deux branches, deux PR), cahier des charges et diagrammes mis à jour dans un commit qui le dit.
4. **V1.0** — jalon `[JALON] v1.0`, `CHANGELOG.md` cohérent avec l'historique, README testé depuis un clone vierge, backlog restant trié.
5. **Soumission** — `SOUMISSION.md` rempli (adresse de **ce seul dépôt** et hash complet sur 40 caractères) puis téléversé sur la plateforme.

**Definition of Done : une issue est terminée quand** — le code est poussé sur sa branche, la PR lie l'issue (issue fermée par le commit), les règles RGx concernées sont appliquées côté serveur, le build backend et frontend passe, et `main` reste fonctionnel après fusion.

**Definition of Done : une issue est terminée quand** — le code est poussé sur sa branche, la PR lie l'issue (issue fermée par le commit), les règles RGx concernées sont appliquées côté serveur, le build backend et frontend passe, et `main` reste fonctionnel après fusion.
