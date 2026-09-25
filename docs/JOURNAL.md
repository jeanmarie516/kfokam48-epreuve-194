# Journal de bord — K48 Présences & Relectures

Une entrée par étape : ce que je viens de faire, ce qui m'a bloqué et combien de temps, ce que j'ai demandé à l'IA et comment j'ai vérifié sa réponse.

## Étape 1 — Analyse et conception

**Fait :** lecture complète du sujet, du LISEZ-MOI et des 16 réponses du client ; cahier des charges (16 EF, 15 RG, 6 RNF, 5 décisions/contradictions tranchées) ; 4 diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence présence, D4 bonus états d'un exercice) ; contrat d'API complété (5 opérations imposées + 11 complémentaires) ; .gitignore posé avant tout code ; dépôt créé et `[JALON] depart` poussé pour vérifier l'accès.

**Backlog :** 14 issues créées (9 Must, 4 Should, 1 Could), chacune avec critères d'acceptation vérifiables et renvois EFx/RGx. L'IA en avait proposé 18 ; j'ai retiré celles qui étaient des tâches techniques et non des résultats, puis réintégré le chantier technique de base (schéma + erreurs) car il est explicitement noté via B3–B5.

**Bloqué :** rien de matériel. ~40 min de réflexion pour identifier le trou que personne n'a vu — c'est le **moment du tirage du relecteur** (aucune question client ne le précise). S'il avait lieu à la clôture, Q10 (corriger avant clôture) serait impraticable : décision D2 — tirage au dépôt, retenté à chaque nouvelle présence.

## Étape 2 — Première version (v0.1)

**Fait :** 7 branches/PR fusionnées (#15 à #22), une par ticket, issues fermées par les commits. Backend : squelette Flyway + erreurs { code, message }, sessions, présences (RG1/RG2/RG3/RG13), exercices, relectures (D1 : POST + PUT jusqu'à clôture), tableau Q16, référentiels, CORS, données de démo. Frontend React : 3 écrans (F2), couche API dédiée (F3), build vérifié (F1). 8 tests verts (B6) : 4 unitaires sur RG5/RG9/D1/RG10, 4 d'intégration sur POST /api/presences et GET /api/tableau sur H2.

**Bloqué :** ~15 min — @MockitoBean n'existe pas en Spring Boot 3.3, remplacé par @MockBean ; et le test a révélé un vrai bug d'ordre : un déposant tentant de relire son propre exercice recevait 404 au lieu du 403 attendu par le contrat — RG5 est maintenant vérifié en premier.

**IA :** a généré le premier jet des services et des composants React ; vérifié par revue ligne à ligne des règles RGx contre le cahier des charges, exécution des 8 tests, et test manuel du flux (session → présence → dépôt → relecture → tableau) via les écrans.

## Étape 4 — Livraison finale (v1.0)

**Fait :** README d'installation (F1 : React justifié en une ligne), CHANGELOG cohérent avec les PR, docker compose (db + backend + frontend), tickets restants #2, #10, #14 fermés, backlog trié (0 issue Must ouverte). Test du démarrage comme le sujet l'exige : `docker compose up` vérifié sur cette machine (ports surchargés via .env local, valeurs par défaut standards pour le correcteur), puis test de fumée des 5 opérations imposées de bout en bout — toutes les réponses conformes au contrat, moyenne calculée par l'API visible dans le tableau.

**Bloqué :** ~35 min — deux bugs révélés par le test du démarrage et jamais vus avant : (1) le test d'intégration s'appelait `*IT` et surefire ne l'exécutait pas, il ne tournait donc jamais — renommé en `*Test`, ce qui a révélé (2) une requête dérivée invalide (`etudiantId` au lieu de `depositaireId`) qui aurait fait planter le démarrage chez le correcteur. Le test de fumée complet passe après correction. Les ports 5432/8080 étant occupés par d'autres projets locaux, ils sont devenus paramétrables (K48_DB_PORT/K48_API_PORT/K48_FRONT_PORT).

**IA :** a aidé à diagnostiquer la pile de logs Docker (requête dérivée Spring Data) et à structurer le CHANGELOG ; vérifié en relançant les 9 tests locaux puis le scénario complet curl sur la pile réelle, en comparant chaque réponse au contrat d'API.

### Repriorisation écrite

L'enveloppe de l'étape 3 n'étant pas encore remise à la livraison, la v1.0 est posée d'abord comme convenu avec le client de l'épreuve ; l'enveloppe (bug + changement de besoin) sera traitée dès réception avec la conduite du changement exigée : issue avant de coder, bug reproduit, migration versionnée, contrat mis à jour, correctif et évolution séparés, analyse mise à jour dans un commit qui le dit.

## Étape 3 — Enveloppe (bug + changement de besoin)

**Fait :** 4 issues créées AVANT tout code (#24 bug, #25/#26 changement, #27 sacrifice). Bug : traduction du témoignage client en course sur le check-then-insert des présences ; test d'intégration concurrent committé ROUGE (CONFLIT reçu au lieu de DEJA_PRESENT), puis correctif dans GlobalExceptionHandler (violation de uq_presence → 409 DEJA_PRESENT), test passé au commit suivant — la preuve rouge→vert est dans l'historique. Changement : analyse mise à jour d'abord dans un commit dédié qui le dit (RG6 réécrite, RG16–RG18, D3/D4, sacrifice S1 : EF10 sort du périmètre maintenu), contrat v1.1.0, migration V3 ajoutée sans toucher V1/V2 (unicité (exercice, relecteur), données existantes préservées), tirage de 2 relecteurs distincts complété à chaque présence, note retenue = moyenne au dixième / provisoire / en attente. Deux branches, deux PR séparées (fix/course-presences puis feature/deux-relecteurs). 13 tests verts.

**Bloqué :** ~25 min — la première V3 utilisait du plpgsql (spécifique PostgreSQL) qui casse les tests H2 ; réécrite en SQL portable (DROP ... IF EXISTS + index unique composite), le plafond « 2 max » étant garanti applicativement. En passant, le renommage du test d'intégration IT→Test a déjà révélé à l'étape 4 que ces tests ne tournaient jamais.

**IA :** a proposé le diagnostic de concurrence et la structure du correctif ; vérifié en écrivant moi-même le test qui reproduit le symptôme exact du client (il devait échouer pour la bonne raison : CONFLIT ≠ DEJA_PRESENT), en relisant la V3 contre la règle « données existantes préservées », et en rejouant les 13 tests. Le sacrifice S1 et les décisions D3/D4 sont les miens, écrits dans le cahier des charges.

**IA :** utilisée pour le formalisme Mermaid (syntaxe `stateDiagram-v2`, `erDiagram`) et pour relire la cohérence EF↔RG↔contrat. Vérifié en relisant chaque diagramme contre les codes HTTP du contrat d'API imposé (410/409/403/429, format d'erreur unique { code, message }), chaque colonne de D2 contre les futures migrations Flyway, et chaque règle RGx contre la question client d'origine.
