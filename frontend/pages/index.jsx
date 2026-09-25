import { useState } from 'react'
import EcranFormateur from '../src/components/EcranFormateur.jsx'
import EcranEtudiant from '../src/components/EcranEtudiant.jsx'
import EcranRelecteur from '../src/components/EcranRelecteur.jsx'

const ONGLETS = [
  { id: 'formateur', label: 'Formateur', icone: 'school' },
  { id: 'etudiant', label: 'Étudiant', icone: 'menu_book' },
  { id: 'relecteur', label: 'Relecteur', icone: 'rate_review' },
]

export default function Accueil() {
  const [onglet, setOnglet] = useState('formateur')

  return (
    <>
      <header className="topbar">
        <div className="topbar__inner">
          <div className="brand">
            <div className="brand__mark">K48</div>
            <div>
              <div className="brand__title">Présences &amp; Relectures</div>
              <div className="brand__sub">Direction de la formation · KFOKAM48</div>
            </div>
          </div>
          <div className="topbar__meta">
            Suivi des sessions, dépôts et relectures par les pairs
          </div>
        </div>
      </header>

      <nav className="nav-tabs" aria-label="Espaces">
        {ONGLETS.map((o) => (
          <button
            key={o.id}
            className={onglet === o.id ? 'active' : ''}
            onClick={() => setOnglet(o.id)}
            aria-pressed={onglet === o.id}
          >
            <span className="icone icone--petit" aria-hidden>{o.icone}</span> {o.label}
          </button>
        ))}
      </nav>

      <main className="conteneur">
        {onglet === 'formateur' && <EcranFormateur />}
        {onglet === 'etudiant' && <EcranEtudiant />}
        {onglet === 'relecteur' && <EcranRelecteur />}
      </main>

      <footer className="pied">
        K48 — Présences &amp; Relectures · Épreuve finale fullstack KFOKAM48
      </footer>
    </>
  )
}
