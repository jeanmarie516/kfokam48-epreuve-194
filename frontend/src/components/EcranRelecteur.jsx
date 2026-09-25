import { useEffect, useState } from 'react'
import { api } from '../api/client.js'

/**
 * Écran relecteur (F2) : choisir son nom (Q1), voir ses relectures assignées,
 * rendre une note entière sur 20 (RG9) + commentaire, corriger avant clôture
 * (RG10/D1). RG5 est appliqué côté serveur : impossible de relire son propre exercice.
 */
export default function EcranRelecteur() {
  const [promotions, setPromotions] = useState([])
  const [promotionId, setPromotionId] = useState('')
  const [etudiants, setEtudiants] = useState([])
  const [etudiantId, setEtudiantId] = useState('')
  const [relectures, setRelectures] = useState([])
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    api.promotions().then(setPromotions).catch((e) => setErreur(e))
  }, [])

  useEffect(() => {
    if (!promotionId) return
    api.etudiants(promotionId).then(setEtudiants).catch((e) => setErreur(e))
  }, [promotionId])

  useEffect(() => {
    if (!etudiantId) return
    rafraichir()
  }, [etudiantId])

  async function rafraichir() {
    setChargement(true)
    setErreur(null)
    try {
      setRelectures(await api.relecturesAFaire(Number(etudiantId)))
    } catch (e) {
      setErreur(e)
    } finally {
      setChargement(false)
    }
  }

  async function soumettre(relecture, note, commentaire) {
    setErreur(null)
    setMessage(null)
    setChargement(true)
    try {
      if (relecture.rendue) {
        await api.corrigerRelecture(relecture.relectureId, Number(etudiantId), note, commentaire)
        setMessage('Relecture corrigée (possible tant que la session n’est pas clôturée).')
      } else {
        await api.rendreRelecture(relecture.relectureId, Number(etudiantId), note, commentaire)
        setMessage('Relecture rendue : vous pouvez la corriger jusqu’à la clôture de la session.')
      }
      await rafraichir()
    } catch (e) {
      setErreur(e)
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
        Je suis le relecteur :{' '}
        <select value={etudiantId} onChange={(e) => setEtudiantId(e.target.value)} disabled={!promotionId}>
          <option value="">— choisir mon nom —</option>
          {etudiants.map((et) => (
            <option key={et.id} value={et.id}>{et.nom}</option>
          ))}
        </select>
      </label>

      {etudiantId && (
        <section style={{ marginTop: 16 }}>
          <h2>
            Mes relectures{' '}
            <button onClick={rafraichir} disabled={chargement}>Rafraîchir</button>
          </h2>
          {chargement && <p>Chargement…</p>}
          {erreur && <p style={{ color: 'red' }}>{erreur.code} : {erreur.message}</p>}
          {message && <p style={{ color: 'green' }}>{message}</p>}
          {relectures.length === 0 && !chargement && (
            <p>Aucune relecture assignée. Un tirage au sort vous désignera parmi les présents (RG7).</p>
          )}
          {relectures.map((r) => (
            <FormulaireRelecture key={r.relectureId} relecture={r} onSoumettre={soumettre} chargement={chargement} />
          ))}
        </section>
      )}
    </div>
  )
}

function FormulaireRelecture({ relecture, onSoumettre, chargement }) {
  const [note, setNote] = useState(relecture.rendue && relecture.note != null ? String(relecture.note) : '')
  const [commentaire, setCommentaire] = useState(relecture.commentaire || '')

  return (
    <div style={{ border: '1px solid #ccc', padding: 8, marginBottom: 8 }}>
      <div>
        Exercice #{relecture.exerciceId} —{' '}
        <a href={relecture.lien} target="_blank" rel="noreferrer">{relecture.lien}</a>
      </div>
      <form
        onSubmit={(e) => {
          e.preventDefault()
          onSoumettre(relecture, Number(note), commentaire)
        }}
      >
        <label>
          Note (entière, 0–20, RG9):{' '}
          <input
            type="number"
            min="0"
            max="20"
            step="1"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            required
          />
          /20
        </label>{' '}
        <label>
          Commentaire:{' '}
          <input value={commentaire} onChange={(e) => setCommentaire(e.target.value)} size={50} required />
        </label>{' '}
        <button type="submit" disabled={chargement}>
          {relecture.rendue ? 'Corriger ma relecture' : 'Rendre ma relecture'}
        </button>
      </form>
    </div>
  )
}
