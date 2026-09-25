package com.kfokam48.presences.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Relecture d'un exercice : une seule par exercice (RG6). La note est un entier 0–20 (RG9).
 * D1 : la relecture est soumise une fois (POST), corrigée jusqu'à la clôture de session (RG10).
 */
@Entity
@Table(name = "relecture", uniqueConstraints = @UniqueConstraint(columnNames = "exercice_id"))
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercice_id", nullable = false)
    private Exercice exercice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relecteur_id", nullable = false)
    private Etudiant relecteur;

    @Column
    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Column(nullable = false)
    private boolean rendue = false;

    @Column(name = "rendue_at")
    private Instant rendueAt;

    protected Relecture() {
    }

    /** Créée à l'assignation (tirage D2) : elle n'est pas encore rendue. */
    public Relecture(Exercice exercice, Etudiant relecteur) {
        this.exercice = exercice;
        this.relecteur = relecteur;
    }

    /** RG9 : note entière 0–20. Lève NoteInvalideException sinon. */
    public void rendre(int note, String commentaire) {
        if (note < 0 || note > 20) {
            throw new NoteInvalideException();
        }
        if (rendue) {
            throw new RelectureDejaRendueException();
        }
        this.note = note;
        this.commentaire = commentaire;
        this.rendue = true;
        this.rendueAt = Instant.now();
        exercice.marquerRelu();
    }

    /** RG10/D1 : correction possible jusqu'à la clôture de la session. */
    public void corriger(int note, String commentaire) {
        if (note < 0 || note > 20) {
            throw new NoteInvalideException();
        }
        this.note = note;
        this.commentaire = commentaire;
    }

    public Long getId() {
        return id;
    }

    public Exercice getExercice() {
        return exercice;
    }

    public Etudiant getRelecteur() {
        return relecteur;
    }

    public Integer getNote() {
        return note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public boolean estRendue() {
        return rendue;
    }

    public Instant getRendueAt() {
        return rendueAt;
    }

    public static class NoteInvalideException extends RuntimeException {
    }

    public static class RelectureDejaRendueException extends RuntimeException {
    }
}
