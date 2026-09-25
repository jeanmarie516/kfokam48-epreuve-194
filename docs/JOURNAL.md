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

**IA :** utilisée pour le formalisme Mermaid (syntaxe `stateDiagram-v2`, `erDiagram`) et pour relire la cohérence EF↔RG↔contrat. Vérifié en relisant chaque diagramme contre les codes HTTP du contrat d'API imposé (410/409/403/429, format d'erreur unique { code, message }), chaque colonne de D2 contre les futures migrations Flyway, et chaque règle RGx contre la question client d'origine.
