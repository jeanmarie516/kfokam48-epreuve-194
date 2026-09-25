package com.kfokam48.presences.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Exercice déposé par un étudiant pour une session : unicité (session, étudiant)
 * imposée par le contrat (409 exercice déjà déposé). Cycle de vie : EN_ATTENTE → RELU (D4).
 */
@Entity
@Table(name = "exercice", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Exercice {

    public static final String EN_ATTENTE = "EN_ATTENTE";
    public static final String RELU = "RELU";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant depositaire;

    @Column(nullable = false, length = 500)
    private String lien;

    @Column(nullable = false, length = 12)
    private String statut = EN_ATTENTE;

    @Column(name = "depose_at", nullable = false)
    private Instant deposeAt;

    protected Exercice() {
    }

    public Exercice(SessionCours session, Etudiant depositaire, String lien, Instant deposeAt) {
        this.session = session;
        this.depositaire = depositaire;
        this.lien = lien;
        this.deposeAt = deposeAt;
    }

    /** RG8 : le lien est verrouillé dès que l'exercice a été relu. */
    public void remplacerLien(String nouveauLien) {
        if (RELU.equals(statut)) {
            throw new LienVerrouilleException();
        }
        this.lien = nouveauLien;
    }

    public void marquerRelu() {
        this.statut = RELU;
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getDepositaire() {
        return depositaire;
    }

    public String getLien() {
        return lien;
    }

    public String getStatut() {
        return statut;
    }

    public Instant getDeposeAt() {
        return deposeAt;
    }

    /** RG8 : le lien ne peut plus être remplacé après relecture. */
    public static class LienVerrouilleException extends RuntimeException {
    }
}
