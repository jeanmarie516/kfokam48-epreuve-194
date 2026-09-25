# Soumission — Épreuve finale fullstack KFOKAM48

> ⚠️ Ce fichier est téléversé sur la plateforme **avant 18h00**. C'est l'étape 5 qui valide l'examen.
> ⚠️ Il ne reste **qu'un seul dépôt** (le sujet corrigé supprime l'épreuve Git sur bundle).

Nom et prénom(s) : **Jean-Marie KAREMAMANA**
Matricule : **194** (dépôt : `kfokam48-epreuve-194`)
Centre : **Yaoundé**

## Projet

Dépôt GitHub (public) : https://github.com/jeanmarie516/kfokam48-epreuve-194

Commit final (hash complet sur 40 caractères) : **e6ea70052f99a067b0a09f328289e3e1ac02f1ef**

> ⚠️ À vérifier au moment du téléversement : ce champ est le hash du **dernier commit poussé de `main`**. Il correspond au commit `e6ea700` (design responsive tous écrans + icônes Material Icons). Relevez-le depuis l'onglet « Commits » de GitHub (bouton de copie) juste **avant** le téléversement, et remplacez-le ici si besoin. Tout ce qui est poussé après le hash déclaré est ignoré par le correcteur.

## Divers

Frontend choisi : **Next.js**

Commande de démarrage :
```bash
# Option 1 (recommandée) : une seule commande
docker compose up --build
# → frontend http://localhost:5173 · API http://localhost:8080

# Option 2 : trois commandes
docker run -d --name k48-db -e POSTGRES_USER=k48 -e POSTGRES_PASSWORD=k48 -e POSTGRES_DB=k48 -p 5432:5432 postgres:16
cd backend && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev
```

## Points à comprendre (check-list avant envoi)

1. Vérifier que le dépôt est **public** : ouvrir le lien en **navigation privée** → il doit charger sans connexion.
2. Copier le **hash complet (40 caractères)** du dernier commit poussé de `main`.
3. Ne pas supprimer / passer en privé le dépôt jusqu'à la publication des résultats.
4. Sans ce fichier téléversé, rien n'est rendu, même si le code est parfait sur GitHub.