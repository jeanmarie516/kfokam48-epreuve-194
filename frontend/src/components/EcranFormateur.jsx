import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

/**
 * Écran formateur (F2) : ouvrir une session (reçoit le code), ajouter une présence
 * à la main (RG4/Q14), clôturer, voir le tableau (Q16).
 */
export default function EcranFormateur() {
  const [promotions, setPromotions] = useState([])
  const [promotionId, setPromotionId] = useState('')
  const [titre, setTitre] = useState('')
  const [sessionOuverte, setSessionOuverte] = useState(null)
  const [etudiants, setEtudiants] = useState([])
  const [etudiantPresence, setEtudiantPresence] = useState('')
  const [tableau, setTableau] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    api.promotions().then(setPromotions).catch((e) => setErreur(e))
  }, [])

  useEffect(() => {
    if (!promotionId) return
    api.etudiants(promotionId).then(setEtudiants).catch((e) => setErreur(e))
    rafraichirTableau(promotionId)
  }, [promotionId])

  async function rafraichirTableau(pid = promotionId) {
    if (!pid) return
    setChargement(true)
    setErreur(null)
    try {
      setTableau(await api.tableau(pid))
    } catch (e) {
      setErreur(e)
    } finally {
      setChargement(false)
    }
  }

  async function ouvrirSession(e) {
    e.preventDefault()
    setErreur(null)
    setMessage(null)
    setChargement(true)
    try {
      const session = await api.ouvrirSession(titre, Number(promotionId))
      setSessionOuverte(session)
      setTitre('')
      await rafraichirTableau()
    } catch (err) {
      setErreur(err)
    } finally {
      setChargement(false)
    }
  }

  async function ajouterPresence(e) {
    e.preventDefault()
    setErreur(null)
    setMessage(null)
    try {
      await api.ajouterPresence(sessionOuverte.id, Number(etudiantPresence))
      setMessage('Présence ajoutée (source FORMATEUR).')
      setEtudiantPresence('')
    } catch (err) {
      setErreur(err)
    }
  }

  async function cloturer() {
    setErreur(null)
    setMessage(null)
    try {
      await api.cloturer(sessionOuverte.id)
      setMessage('Session clôturée : plus aucun dépôt ni correction acceptés.')
      setSessionOuverte({ ...sessionOuverte, cloturee: true })
    } catch (err) {
      setErreur(err)
    }
  }

  return (
    <div>
      <section>
        <h2>Ouvrir une session</h2>
        <form onSubmit={ouvrirSession}>
          <label>
            Promotion:{' '}
            <select value={promotionId} onChange={(e) => setPromotionId(e.target.value)} required>
              <option value="">— choisir —</option>
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>{p.nom}</option>
              ))}
            </select>
          </label>{' '}
          <label>
            Titre:{' '}
            <input value={titre} onChange={(e) => setTitre(e.target.value)} placeholder="Java 101" required />
          </label>{' '}
          <button type="submit" disabled={chargement || !promotionId}>Ouvrir</button>
        </form>
        {sessionOuverte && (
          <div style={{ marginTop: 8, padding: 8, border: '1px solid #888' }}>
            <strong>Code de présence : {sessionOuverte.code}</strong>
            <div>
              Expire à {new Date(sessionOuverte.expirationAt).toLocaleTimeString()} (RG1 : +15 min)
              {sessionOuverte.cloturee ? ' · CLÔTURÉE' : ''}
            </div>
            {!sessionOuverte.cloturee && (
              <>
                <form onSubmit={ajouterPresence} style={{ marginTop: 8 }}>
                  <label>
                    Ajouter une présence à la main (Q14):{' '}
                    <select value={etudiantPresence} onChange={(e) => setEtudiantPresence(e.target.value)} required>
                      <option value="">— étudiant —</option>
                      {etudiants.map((et) => (
                        <option key={et.id} value={et.id}>{et.nom}</option>
                      ))}
                    </select>
                  </label>{' '}
                  <button type="submit">Ajouter</button>
                </form>
                <button onClick={cloturer} style={{ marginTop: 8 }}>Clôturer la session</button>
              </>
            )}
          </div>
        )}
        {message && <p style={{ color: 'green' }}>{message}</p>}
      </section>

      <section style={{ marginTop: 24 }}>
        <h2>
          Tableau{' '}
          <button onClick={() => rafraichirTableau()} disabled={chargement}>Rafraîchir</button>
        </h2>
        {chargement && <p>Chargement…</p>}
        {erreur && <p style={{ color: 'red' }}>{erreur.code} : {erreur.message}</p>}
        {tableau.length > 0 && (
          <table border="1" cellPadding="6">
            <thead>
              <tr>
                <th>Étudiant</th>
                <th>Sessions / présence</th>
                <th>Exercices déposés</th>
                <th>Moyenne reçue</th>
                <th>Relectures en attente</th>
              </tr>
            </thead>
            <tbody>
              {tableau.map((ligne) => (
                <tr key={ligne.etudiantId}>
                  <td>{ligne.nom}</td>
                  <td>
                    {ligne.presences.filter(Boolean).length === 0
                      ? '—'
                      : ligne.presences.map((p, i) =>
                          p ? (
                            <span key={i}> · S{p.sessionId} ({p.source}) </span>
                          ) : null,
                        )}
                  </td>
                  <td>{ligne.exercicesDeposes}</td>
                  {/* F3 : la moyenne vient de l'API, jamais recalculée ici */}
                  <td>{ligne.moyenne == null ? '—' : `${ligne.moyenne}/20`}</td>
                  <td>{ligne.relecturesEnAttente > 0 ? ligne.relecturesEnAttente : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  )
}
