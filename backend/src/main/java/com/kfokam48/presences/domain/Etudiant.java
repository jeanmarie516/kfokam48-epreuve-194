package com.kfokam48.presences.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false)
    private String nom;

    protected Etudiant() {
    }

    public Etudiant(Promotion promotion, String nom) {
        this.promotion = promotion;
        this.nom = nom;
    }

    public Long getId() {
        return id;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getNom() {
        return nom;
    }
}
