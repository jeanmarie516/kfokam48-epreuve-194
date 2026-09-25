package com.kfokam48.presences.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Présence d'un étudiant à une session. RG2 : unicité (session, étudiant).
 * RG4 : source = ETUDIANT (saisie par le code) ou FORMATEUR (ajout manuel, Q14).
 */
@Entity
@Table(name = "presence", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Presence {

    public enum Source { ETUDIANT, FORMATEUR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private SessionCours session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Etudiant etudiant;

    @Column(nullable = false, length = 12)
    private String source;

    @Column(name = "creee_at", nullable = false)
    private Instant creeeAt;

    protected Presence() {
    }

    private Presence(SessionCours session, Etudiant etudiant, Source source, Instant creeeAt) {
        this.session = session;
        this.etudiant = etudiant;
        this.source = source.name();
        this.creeeAt = creeeAt;
    }

    public static Presence parEtudiant(SessionCours session, Etudiant etudiant, Instant creeeAt) {
        return new Presence(session, etudiant, Source.ETUDIANT, creeeAt);
    }

    /** Q14 : ajout manuel par le formateur, tracé. */
    public static Presence parFormateur(SessionCours session, Etudiant etudiant, Instant creeeAt) {
        return new Presence(session, etudiant, Source.FORMATEUR, creeeAt);
    }

    public Long getId() {
        return id;
    }

    public SessionCours getSession() {
        return session;
    }

    public Etudiant getEtudiant() {
        return etudiant;
    }

    public Source getSource() {
        return Source.valueOf(source);
    }

    public Instant getCreeeAt() {
        return creeeAt;
    }
}
