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
      <label>
        Promotion:{' '}
        <select value={promotionId} onChange={(e) => { setPromotionId(e.target.value); setEtudiantId('') }}>
          <option value="">— choisir —</option>
          {promotions.map((p) => (
            <option key={p.id} value={p.id}>{p.nom}</option>
          ))}
        </select>
      </label>{' '}
      <label>
        Je suis :{' '}
        <select value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)} disabled={!promotionId}>
          <option value="">— choisir mon nom —</option>
          {etudiants.map((et) => (
            <option key={et.id} value={et.id}>{et.nom}</option>
          ))}
        </select>
      </label>

      {etudiantId && (
        <>
          <section style={{ marginTop: 16 }}>
            <h2>Marquer ma présence</h2>
            <form onSubmit={marquerPresence}>
              <label>
                Session:{' '}
                <select value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
                  <option value="">— session où déposer ensuite —</option>
                  {sessions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.titre} {s.cloturee ? '(clôturée)' : ''}
                    </option>
                  ))}
                </select>
              </label>{' '}
              <label>
                Code reçu:{' '}
                <input value={code} onChange={(e) => setCode(e.target.value)} maxLength={6} required />
              </label>{' '}
              <button type="submit" disabled={chargement}>Je suis présent</button>
            </form>
          </section>

          <section style={{ marginTop: 16 }}>
            <h2>Déposer mon exercice</h2>
            <form onSubmit={deposer}>
              <label>
                Session:{' '}
                <select value={sessionId} onChange={(e) => setSessionId(e.target.value)}>
                  <option value="">— choisir —</option>
                  {sessions.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.titre} {s.cloturee ? '(clôturée)' : ''}
                    </option>
                  ))}
                </select>
              </label>{' '}
              <label>
                Lien:{' '}
                <input
                  value={lien}
                  onChange={(e) => setLien(e.target.value)}
                  placeholder="https://github.com/moi/exo"
                  size={40}
                  required
                />
              </label>{' '}
              <button type="submit" disabled={chargement || !sessionId}>Déposer</button>
            </form>
          </section>

          <section style={{ marginTop: 16 }}>
            <h2>Mes exercices et notes reçues</h2>
            {chargement && <p>Chargement…</p>}
            {erreur && <p style={{ color: 'red' }}>{erreur.code} : {erreur.message}</p>}
            {message && <p style={{ color: 'green' }}>{message}</p>}
            {mesExercices.length === 0 && !chargement && <p>Aucun exercice déposé.</p>}
            {mesExercices.map((ex) => (
              <div key={ex.id} style={{ border: '1px solid #ccc', padding: 8, marginBottom: 8 }}>
                <div>
                  Session {ex.sessionId} — <strong>{ex.statut}</strong>{' '}
                  <a href={ex.lien} target="_blank" rel="noreferrer">{ex.lien}</a>
                </div>
                {/* S1 (enveloppe) : le remplacement du lien est sorti du périmètre maintenu —
                    avec deux relecteurs, remplacer un lien après le premier rendu crée une incohérence. */}
                {ex.noteRetenue ? (
                  <div style={{ marginTop: 4 }}>
                    Note reçue : <strong>{ex.noteRetenue.valeur}/20</strong>
                    {ex.noteRetenue.provisoire && (
                      <em> (provisoire — une seule des deux relectures rendues)</em>
                    )}
                    {ex.noteRetenue.commentaires.map((c, i) => (
                      <div key={i}>« {c} »</div>
                    ))}
                    {/* RG14 : les noms des relecteurs ne sont jamais affichés, l'API ne les renvoie pas */}
                  </div>
                ) : (
                  <div style={{ marginTop: 4, color: '#666' }}>
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
