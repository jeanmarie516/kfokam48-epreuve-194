# Journal de bord — K48 Présences & Relectures

Une entrée par étape : ce que je viens de faire, ce qui m'a bloqué et combien de temps, ce que j'ai demandé à l'IA et comment j'ai vérifié sa réponse.

## Étape 1 — Analyse et conception

**Fait :** lecture complète du sujet, du LISEZ-MOI et des 16 réponses du client ; cahier des charges (16 EF, 15 RG, 6 RNF, 5 décisions/contradictions tranchées) ; 4 diagrammes Mermaid (D1 cas d'utilisation, D2 modèle de données, D3 séquence présence, D4 bonus états d'un exercice) ; contrat d'API complété (5 opérations imposées + 11 complémentaires) ; .gitignore posé avant tout code ; dépôt créé et `[JALON] depart` poussé pour vérifier l'accès.

**Bloqué :** rien de matériel. ~40 min de réflexion pour identifier le trou que personne n'a vu — c'est le **moment du tirage du relecteur** (aucune question client ne le précise). S'il avait lieu à la clôture, Q10 (corriger avant clôture) serait impraticable : décision D2 — tirage au dépôt, retenté à chaque nouvelle présence.

**IA :** utilisée pour le formalisme Mermaid (syntaxe `stateDiagram-v2`, `erDiagram`) et pour relire la cohérence EF↔RG↔contrat. Vérifié en relisant chaque diagramme contre les codes HTTP du contrat d'API imposé (410/409/403/429, format d'erreur unique { code, message }), chaque colonne de D2 contre les futures migrations Flyway, et chaque règle RGx contre la question client d'origine.
