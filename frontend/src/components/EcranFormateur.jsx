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
      <section className="carte">
        <div className="carte__tete">
          <h2 className="carte__titre">Ouvrir une session</h2>
          <span className="badge badge--indigo">Code valable 15 min</span>
        </div>
        <form className="formulaire" onSubmit={ouvrirSession}>
          <div className="champ">
            <label htmlFor="f-promotion">Promotion</label>
            <select
              id="f-promotion"
              value={promotionId}
              onChange={(e) => setPromotionId(e.target.value)}
              required
            >
              <option value="">— choisir —</option>
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>{p.nom}</option>
              ))}
            </select>
          </div>
          <div className="champ">
            <label htmlFor="f-titre">Titre</label>
            <input
              id="f-titre"
              value={titre}
              onChange={(e) => setTitre(e.target.value)}
              placeholder="Java 101"
              required
            />
          </div>
          <button type="submit" className="bouton bouton--principal" disabled={chargement || !promotionId}>
            Ouvrir la session
          </button>
        </form>

        {sessionOuverte && (
          <div style={{ marginTop: 18 }}>
            <div className="code-box">
              <div>
                <div className="code-box__label">CODE DE PRÉSENCE</div>
                <div className="code-box__valeur">{sessionOuverte.code}</div>
              </div>
              <div className="code-box__expire">
                Expire à {new Date(sessionOuverte.expirationAt).toLocaleTimeString()} (RG1 : +15 min)
                <br />
                {sessionOuverte.cloturee ? <strong>Session clôturée</strong> : 'À communiquer aux étudiants'}
              </div>
            </div>

            {!sessionOuverte.cloturee && (
              <>
                <form className="formulaire" onSubmit={ajouterPresence}>
                  <div className="champ">
                    <label htmlFor="f-presence">Ajouter une présence à la main (Q14)</label>
                    <select
                      id="f-presence"
                      value={etudiantPresence}
                      onChange={(e) => setEtudiantPresence(e.target.value)}
                      required
                    >
                      <option value="">— étudiant —</option>
                      {etudiants.map((et) => (
                        <option key={et.id} value={et.id}>{et.nom}</option>
                      ))}
                    </select>
                  </div>
                  <button type="submit" className="bouton bouton--secondaire">Ajouter</button>
                  <button type="button" className="bouton bouton--danger" onClick={cloturer}>
                    Clôturer la session
                  </button>
                </form>
              </>
            )}
          </div>
        )}
        {message && <div className="notice notice--succes">✓ {message}</div>}
      </section>

      <section className="carte">
        <div className="carte__tete">
          <h2 className="carte__titre">Tableau de suivi</h2>
          <button className="bouton bouton--secondaire" onClick={() => rafraichirTableau()} disabled={chargement}>
            ⟳ Rafraîchir
          </button>
        </div>
        {chargement && <div className="notice notice--info">Chargement…</div>}
        {erreur && (
          <div className="notice notice--erreur">
            ⚠ <code>{erreur.code}</code> — {erreur.message}
          </div>
        )}
        {tableau.length > 0 && (
          <div className="tableau-enveloppe">
            <table className="tableau">
              <thead>
                <tr>
                  <th>Étudiant</th>
                  <th>Présence par session</th>
                  <th>Exercices</th>
                  <th>Moyenne reçue</th>
                  <th>Relectures en attente</th>
                </tr>
              </thead>
              <tbody>
                {tableau.map((ligne) => {
                  const presences = ligne.presences.filter(Boolean)
                  return (
                    <tr key={ligne.etudiantId}>
                      <td><strong>{ligne.nom}</strong></td>
                      <td>
                        {presences.length === 0 ? (
                          <span className="badge badge--gris">—</span>
                        ) : (
                          presences.map((p, i) => (
                            <span
                              key={i}
                              className={p.source === 'FORMATEUR' ? 'badge badge--violet' : 'badge badge--indigo'}
                            >
                              S{p.sessionId} · {p.source === 'FORMATEUR' ? 'manuel' : 'code'}
                            </span>
                          ))
                        )}
                      </td>
                      <td>{ligne.exercicesDeposes > 0
                        ? <span className="badge badge--vert">{ligne.exercicesDeposes} déposé{ligne.exercicesDeposes > 1 ? 's' : ''}</span>
                        : '—'}</td>
                      {/* F3 : la moyenne vient de l'API, jamais recalculée ici */}
                      <td>{ligne.moyenne == null ? '—' : <strong>{ligne.moyenne}/20</strong>}</td>
                      <td>{ligne.relecturesEnAttente > 0
                        ? <span className="badge badge--ambre">{ligne.relecturesEnAttente}</span>
                        : '—'}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
