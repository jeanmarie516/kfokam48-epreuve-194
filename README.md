# K48 — Présences & Relectures

Application fullstack pour la direction de la formation KFOKAM48 : sessions de cours avec code de présence expirant, dépôt de liens d'exercices, relecture par les pairs tirée au sort parmi les présents, et tableau de suivi pour le formateur.

**Frontend choisi : Next.js**, parce qu'il garde la simplicité de React (composants, hooks) tout en apportant un routage intégré et un build de production vérifiable d'une seule commande (`next build`).

## Démarrage

### Option 1 — Docker Compose (une commande)

```bash
docker compose up --build
```

- Frontend : http://localhost:5173
- API : http://localhost:8080
- La base PostgreSQL est créée et les **données de démonstration** (promotion K48, 10 étudiants) sont chargées automatiquement au démarrage.

> **Ports** : les valeurs ci-dessus (5173 / 8080) sont les ports par défaut. Si un port
> est déjà occupé par une autre application, surchargez-le dans un fichier `.env`
> (copiez `.env.example`) — par exemple `K48_FRONT_PORT=5174`, `K48_API_PORT=8081`. Sans
> `.env`, ces valeurs par défaut s'appliquent automatiquement.

### Option 2 — Trois commandes manuelles

Prérequis : Java 17+, Docker (pour la base), Node 18+.

```bash
# 1. Démarrer la base PostgreSQL
docker run -d --name k48-db -e POSTGRES_USER=k48 -e POSTGRES_PASSWORD=k48 -e POSTGRES_DB=k48 -p 5432:5432 postgres:16

# 2. Démarrer le backend (migrations Flyway et données de démo automatiques)
cd backend && ./mvnw spring-boot:run

# 3. Dans un second terminal, démarrer le frontend
cd frontend && npm install && npm run dev
```

Puis ouvrir http://localhost:5173.

### Testé depuis un clone vierge

```bash
git clone https://github.com/jeanmarie516/kfokam48-epreuve-194.git
cd kfokam48-epreuve-194
docker compose up --build
# → http://localhost:5173
```

## Les cinq flux du besoin client

1. **Le formateur ouvre une session** et obtient un code de présence (expire après 15 min).
2. **L'étudiant saisit ce code** pour marquer sa présence (une seule fois, 5 erreurs de code = blocage 2 min).
3. **L'étudiant dépose le lien** de son exercice pour la session (possible jusqu'à la clôture).
4. **Deux relecteurs sont tirés au sort** parmi les présents (règle v2 de l'enveloppe) : ils rendent chacun une note entière sur 20 et un commentaire, corrigeables jusqu'à la clôture ; la note retenue est la moyenne des deux, provisoire si une seule relecture est rendue.
5. **Le formateur voit le tableau** : présence par session, exercices déposés, moyenne des notes reçues, relectures en attente.

## Architecture

```
/api         contrat.yaml          — contrat d'API (5 opérations imposées + compléments)
/backend     Spring Boot 3.3       — Java 17, Maven + mvnw, Flyway, PostgreSQL
/frontend    Next.js 14 (React 18) — 3 écrans (formateur, étudiant, relecteur)
/docs        CAHIER_DES_CHARGES.md — 10 sections, EF1–EF16, RG1–RG15
             JOURNAL.md            — une entrée par étape
             diagrammes/           — D1, D2, D3, D4 en Mermaid
```

## Tests

```bash
cd backend && ./mvnw test
```

13 tests sur H2 en mémoire (aucune base locale requise) :

- **Unitaires** : RG5 (jamais son propre exercice → 403), RG9 (note entière 0–20 → 400 sinon), D1 (soumission unique → 409), RG10 (correction avant clôture), RG16 (moyenne des deux relectures au dixième — enveloppe), RG17 (note provisoire), RG18 (aucune relecture rendue).
- **Intégration** : `POST /api/presences` (201 nominal, 409 `DEJA_PRESENT`, 410 `CODE_EXPIRE` avec le message exact du contrat, 400 `CODE_INCONNU`), `GET /api/tableau` (404 promotion inconnue), course concurrente sur les présences (bug #24 : la perdante reçoit `DEJA_PRESENT`). Vérifie aussi le format d'erreur `{ code, message }` sans stack trace.

## Documentation

- [Cahier des charges](docs/CAHIER_DES_CHARGES.md) — exigences EFx, règles RGx, contradictions tranchées (Q10/Q15 → D1, moment du tirage → D2)
- [Contrat d'API](api/contrat.yaml) — OpenAPI 3.0
- [Journal de bord](docs/JOURNAL.md) — une entrée par étape
- [CHANGELOG](CHANGELOG.md)
- [Soumission (étape 5)](modeles/SOUMISSION.md) — fichier à téléverser sur la plateforme (dépôt unique, hash final)
