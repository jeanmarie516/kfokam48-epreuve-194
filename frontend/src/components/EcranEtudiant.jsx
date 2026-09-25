import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

/**
 * Écran étudiant (F2) : choisir son nom (Q1), marquer sa présence (RG1/RG2),
 * déposer son lien (RG12), remplacer son lien tant que non relu (RG8),
 * voir ses notes reçues sans le nom du relecteur (RG14/Q8).
 */
export default function EcranEtudiant() {
  const [promotions, setPromotions] = useState([])
  const [promotionId, setPromotionId] = useState('')
  const [etudiants, setEtudiants] = useState([])
  const [etudiantId, setEtudiantId] = useState('')
  const [sessions, setSessions] = useState([])
  const [sessionId, setSessionId] = useState('')
  const [code, setCode] = useState('')
  const [lien, setLien] = useState('')
  const [mesExercices, setMesExercices] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    api.promotions().then(setPromotions).catch((e) => setErreur(e))
  }, [])

  useEffect(() => {
    if (!promotionId) return
    api.etudiants(promotionId).then(setEtudiants).catch((e) => setErreur(e))
    api.sessions(promotionId).then(setSessions).catch((e) => setErreur(e))
  }, [promotionId])

  useEffect(() => {
    if (!promotionId || !etudiantId) return
    rafraichirMesExercices()
  }, [promotionId, etudiantId])

  async function rafraichirMesExercices() {
    setChargement(true)
    try {
      setMesExercices(await api.mesExercices(Number(promotionId), Number(etudiantId)))
      setErreur(null)
    } catch (e) {
      setErreur(e)
    } finally {
      setChargement(false)
    }
  }

  async function marquerPresence(e) {
    e.preventDefault()
    setErreur(null)
    setMessage(null)
    setChargement(true)
    try {
      const presence = await api.marquerPresence(code.trim(), Number(etudiantId))
      setMessage(`Présence enregistrée pour la session ${presence.sessionId} (source ${presence.source}).`)
      setCode('')
    } catch (err) {
      setErreur(err)
    } finally {
      setChargement(false)
    }
  }

  async function deposer(e) {
    e.preventDefault()
    setErreur(null)
    setMessage(null)
    setChargement(true)
    try {
      await api.deposerExercice(Number(sessionId), Number(etudiantId), lien.trim())
      setMessage('Exercice déposé : un relecteur va être tiré au sort parmi les présents.')
      setLien('')
      await rafraichirMesExercices()
    } catch (err) {
      setErreur(err)
    } finally {
      setChargement(false)
    }
  }


  return (
    <div>
      <div className="carte">
        <div className="carte__tete">
          <h2 className="carte__titre">Qui êtes-vous ?</h2>
          <span className="badge badge--indigo">Q1 · identité déclarée</span>
        </div>
        <div className="champ--row">
          <div className="champ">
            <label>Promotion</label>
            <select value={promotionId} onChange={(e) => { setPromotionId(e.target.value); setEtudiantId('') }}>
              <option value="">— choisir —</option>
              {promotions.map((p) => (
                <option key={p.id} value={p.id}>{p.nom}</option>
              ))}
            </select>
          </div>
          <div className="champ">
            <label>Je suis</label>
            <select value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)} disabled={!promotionId}>
              <option value="">— choisir mon nom —</option>
              {etudiants.map((et) => (
                <option key={et.id} value={et.id}>{et.nom}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {etudiantId && (
        <>
          <section className="carte">
            <h2 className="carte__titre">Marquer ma présence</h2>
            <form className="formulaire" onSubmit={marquerPresence}>
              <div className="champ" style={{ flex: 1, minWidth: 220 }}>
                <label htmlFor="e-session">Session</label>
                <select id="e-session" value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
                  <option value="">— session —</option>
                  {sessions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.titre} {s.cloturee ? '(clôturée)' : ''}
                    </option>
                  ))}
                </select>
              </div>
              <div className="champ">
                <label htmlFor="e-code">Code reçu</label>
                <input
                  id="e-code"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  maxLength={6}
                  placeholder="6 chiffres"
                  required
                  style={{ letterSpacing: 6, fontFamily: 'var(--mono)', fontWeight: 700 }}
                />
              </div>
              <button type="submit" className="bouton bouton--principal" disabled={chargement}>
                Je suis présent
              </button>
            </form>
          </section>

          <section className="carte">
            <h2 className="carte__titre">Déposer mon exercice</h2>
            <form className="formulaire" onSubmit={deposer}>
              <div className="champ" style={{ flex: 1, minWidth: 220 }}>
                <label htmlFor="e-dep-session">Session</label>
                <select id="e-dep-session" value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
                  <option value="">— choisir —</option>
                  {sessions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.titre} {s.cloturee ? '(clôturée)' : ''}
                    </option>
                  ))}
                </select>
              </div>
              <div className="champ" style={{ flex: 2, minWidth: 280 }}>
                <label htmlFor="e-lien">Lien de l'exercice</label>
                <input
                  id="e-lien"
                  value={lien}
                  onChange={(e) => setLien(e.target.value)}
                  placeholder="https://github.com/moi/exo"
                  required
                />
              </div>
              <button type="submit" className="bouton bouton--principal" disabled={chargement || !sessionId}>
                Déposer
              </button>
            </form>
          </section>

          <section className="carte">
            <div className="carte__tete">
              <h2 className="carte__titre">Mes exercices et notes reçues</h2>
              <span className="badge badge--gris">{mesExercices.length} exercice{mesExercices.length > 1 ? 's' : ''}</span>
            </div>
            {chargement && <div className="notice notice--info">Chargement…</div>}
            {erreur && (
              <div className="notice notice--erreur">⚠ <code>{erreur.code}</code> — {erreur.message}</div>
            )}
            {message && <div className="notice notice--succes">✓ {message}</div>}

            {mesExercices.length === 0 && !chargement && <p className="vide">Aucun exercice déposé.</p>}

            {mesExercices.map((ex) => (
              <div key={ex.id} className="item">
                <div className="item__tete">
                  <div className="item__titre">
                    <span className="badge badge--gris">Session {ex.sessionId}</span>
                    <span className={ex.statut === 'RELU' ? 'badge badge--vert' : 'badge badge--ambre'}>
                      {ex.statut}
                    </span>
                  </div>
                  <a href={ex.lien} target="_blank" rel="noreferrer">{ex.lien}</a>
                </div>
                {ex.noteRetenue ? (
                  <div className="item__note">
                    Note reçue : <strong>{ex.noteRetenue.valeur}/20</strong>{' '}
                    {ex.noteRetenue.provisoire && (
                      <span className="badge badge--ambre">provisoire · 1 relecture rendue / 2</span>
                    )}
                    {ex.noteRetenue.commentaires.map((c, i) => (
                      <p key={i} className="item__commentaire">« {c} »</p>
                    ))}
                    {/* RG14 : les noms des relecteurs ne sont jamais affichés, l'API ne les renvoie pas */}
                  </div>
                ) : (
                  <div className="vide">
                    En attente de relecture (deux relecteurs tirés au sort parmi les présents).
                  </div>
                )}
              </div>
            ))}
          </section>
        </>
      )}
    </div>
  )
}
