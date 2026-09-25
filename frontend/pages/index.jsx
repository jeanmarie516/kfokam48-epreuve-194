import { useState } from 'react'
import EcranFormateur from '../src/components/EcranFormateur.jsx'
import EcranEtudiant from '../src/components/EcranEtudiant.jsx'
import EcranRelecteur from '../src/components/EcranRelecteur.jsx'

const ONGLETS = [
  { id: 'formateur', label: 'Formateur' },
  { id: 'etudiant', label: 'Étudiant' },
  { id: 'relecteur', label: 'Relecteur' },
]

export default function Accueil() {
  const [onglet, setOnglet] = useState('formateur')

  return (
    <div style={{ fontFamily: 'sans-serif', margin: '0 auto', maxWidth: 960, padding: 16 }}>
      <h1>K48 — Présences &amp; Relectures</h1>
      <nav style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        {ONGLETS.map((o) => (
          <button
            key={o.id}
            onClick={() => setOnglet(o.id)}
            style={{
              padding: '8px 16px',
              fontWeight: onglet === o.id ? 'bold' : 'normal',
              cursor: 'pointer',
            }}
          >
            {o.label}
          </button>
        ))}
      </nav>
      {onglet === 'formateur' && <EcranFormateur />}
      {onglet === 'etudiant' && <EcranEtudiant />}
      {onglet === 'relecteur' && <EcranRelecteur />}
    </div>
  )
}
