package com.kfokam48.presences.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Compteur d'erreurs de saisie du code de présence, par étudiant — RG13.
 * Au bout de 5 erreurs consécutives, l'étudiant est bloqué 2 minutes (Q4).
 * Une saisie réussie remet le compteur à zéro.
 */
@Entity
@Table(name = "erreur_saisie_code")
public class ErreurSaisieCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etudiant_id", nullable = false, unique = true)
    private Long etudiantId;

    @Column(name = "nb_erreurs", nullable = false)
    private int nbErreurs = 0;

    @Column(name = "bloque_jusqua")
    private Instant bloqueJusqua;

    protected ErreurSaisieCode() {
    }

    public ErreurSaisieCode(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public void incrementer() {
        this.nbErreurs++;
    }

    public void reset() {
        this.nbErreurs = 0;
        this.bloqueJusqua = null;
    }

    public Long getId() {
        return id;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public int getNbErreurs() {
        return nbErreurs;
    }

    public Instant getBloqueJusqua() {
        return bloqueJusqua;
    }

    public void setBloqueJusqua(Instant bloqueJusqua) {
        this.bloqueJusqua = bloqueJusqua;
    }
}
