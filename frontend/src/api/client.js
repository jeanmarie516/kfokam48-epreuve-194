// F3 — couche d'API dédiée : tout appel HTTP passe par ici, aucun fetch dispersé
// dans les composants. Les erreurs sont normalisées au format { code, message } du contrat.

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function appel(chemin, options = {}) {
  let reponse
  try {
    reponse = await fetch(`${BASE_URL}${chemin}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    })
  } catch {
    // Serveur injoignable : même format d'erreur que le contrat.
    throw { code: 'SERVEUR_INJOIGNABLE', message: 'Impossible de joindre le serveur.' }
  }

  if (reponse.status === 204) return null

  let corps = null
  try {
    corps = await reponse.json()
  } catch {
    corps = null
  }

  if (!reponse.ok) {
    // Le format { code, message } est imposé par le contrat pour toute erreur.
    throw corps && corps.code
      ? corps
      : { code: 'ERREUR_INCONNUE', message: `Erreur HTTP ${reponse.status}` }
  }
  return corps
}

export const api = {
  // Référentiels (sélecteurs d'identité, Q1)
  promotions: () => appel('/api/promotions'),
  etudiants: (promotionId) => appel(`/api/promotions/${promotionId}/etudiants`),
  sessions: (promotionId) => appel(`/api/promotions/${promotionId}/sessions`),

  // Formateur
  ouvrirSession: (titre, promotionId) =>
    appel('/api/sessions', { method: 'POST', body: { titre, promotionId } }),
  cloturer: (sessionId) => appel(`/api/sessions/${sessionId}/cloture`, { method: 'POST' }),
  ajouterPresence: (sessionId, etudiantId) =>
    appel(`/api/sessions/${sessionId}/presences`, { method: 'POST', body: { etudiantId } }),
  exercicesSession: (sessionId) => appel(`/api/sessions/${sessionId}/exercices`),
  tableau: (promotionId) => appel(`/api/tableau?promotionId=${promotionId}`),

  // Étudiant
  marquerPresence: (code, etudiantId) =>
    appel('/api/presences', { method: 'POST', body: { code, etudiantId } }),
  deposerExercice: (sessionId, etudiantId, lien) =>
    appel('/api/exercices', { method: 'POST', body: { sessionId, etudiantId, lien } }),
  remplacerLien: (exerciceId, lien) =>
    appel(`/api/exercices/${exerciceId}/lien`, { method: 'PUT', body: { lien } }),
  mesExercices: (promotionId, etudiantId) =>
    appel(`/api/promotions/${promotionId}/etudiants/${etudiantId}/exercices`),

  // Relecteur
  relecturesAFaire: (relecteurId) => appel(`/api/relectures/a-faire?relecteurId=${relecteurId}`),
  rendreRelecture: (id, relecteurId, note, commentaire) =>
    appel(`/api/relectures/${id}`, { method: 'POST', body: { relecteurId, note, commentaire } }),
  corrigerRelecture: (id, relecteurId, note, commentaire) =>
    appel(`/api/relectures/${id}`, { method: 'PUT', body: { relecteurId, note, commentaire } }),
}
