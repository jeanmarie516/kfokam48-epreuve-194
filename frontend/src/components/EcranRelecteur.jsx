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
            <label>Je suis le relecteur</label>
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
        <section className="carte">
          <div className="carte__tete">
            <h2 className="carte__titre">Mes relectures</h2>
            <button className="bouton bouton--secondaire" onClick={rafraichir} disabled={chargement}>
              <span className="icone icone--petit" aria-hidden>refresh</span> Rafraîchir
            </button>
          </div>
          {chargement && <div className="notice notice--info"><span className="icone icone--petit" aria-hidden>hourglass_top</span> Chargement…</div>}
          {erreur && (
            <div className="notice notice--erreur"><span className="icone icone--petit" aria-hidden>error</span> <code>{erreur.code}</code> — {erreur.message}</div>
          )}
          {message && <div className="notice notice--succes"><span className="icone icone--petit" aria-hidden>check_circle</span> {message}</div>}
          {relectures.length === 0 && !chargement && (
            <p className="vide">
              Aucune relecture assignée. Un tirage au sort vous désignera parmi les présents (RG7).
            </p>
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
    <div className="item">
      <div className="item__tete">
        <div className="item__titre">
          <span className={relecture.rendue ? 'badge badge--vert' : 'badge badge--ambre'}>
            {relecture.rendue ? 'Rendu' : 'À faire'}
          </span>
          <span className="badge badge--gris">Exercice #{relecture.exerciceId}</span>
        </div>
        <a href={relecture.lien} target="_blank" rel="noreferrer">{relecture.lien}</a>
      </div>
      <form
        className="formulaire"
        onSubmit={(e) => {
          e.preventDefault()
          onSoumettre(relecture, Number(note), commentaire)
        }}
      >
        <div className="champ" style={{ minWidth: 150, flexDirection: 'row', alignItems: 'center', gap: 8 }}>
          <label style={{ whiteSpace: 'nowrap' }}>Note (0–20, RG9)</label>
          <input
            type="number"
            min="0"
            max="20"
            step="1"
            value={note}
            onChange={(e) => setNote(e.target.value)}
            style={{ width: 80 }}
            required
          />
          <span className="badge badge--indigo">/ 20</span>
        </div>
        <div className="champ" style={{ flex: 2, minWidth: 260 }}>
          <label>Commentaire</label>
          <input value={commentaire} onChange={(e) => setCommentaire(e.target.value)} placeholder="Avis sur l'exercice…" required />
        </div>
        <button type="submit" className="bouton bouton--principal" disabled={chargement}>
          {relecture.rendue
            ? <><span className="icone icone--petit" aria-hidden>edit</span> Corriger ma relecture</>
            : <><span className="icone icone--petit" aria-hidden>rate_review</span> Rendre ma relecture</>}
        </button>
      </form>
    </div>
  )
}
