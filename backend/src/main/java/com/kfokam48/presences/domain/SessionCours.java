package com.kfokam48.presences.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Une session de cours : ouverte par le formateur, avec un code de présence
 * qui expire (RG1) et une clôture explicite (RG15).
 */
@Entity
@Table(name = "session")
public class SessionCours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false, unique = true, length = 6)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private Instant ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(nullable = false)
    private boolean cloturee = false;

    protected SessionCours() {
    }

    public SessionCours(Promotion promotion, String titre, String code, Instant ouvertureAt, Instant expirationAt) {
        this.promotion = promotion;
        this.titre = titre;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
    }

    /** RG1 : le code expire 15 minutes après l'ouverture. */
    public boolean codeExpire(Instant maintenant) {
        return maintenant.isAfter(expirationAt);
    }

    /** RG3/RG15 : une session clôturée n'accepte plus rien. */
    public boolean estCloturee() {
        return cloturee;
    }

    public void cloturer() {
        this.cloturee = true;
    }

    public Long getId() {
        return id;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public String getTitre() {
        return titre;
    }

    public String getCode() {
        return code;
    }

    public Instant getOuvertureAt() {
        return ouvertureAt;
    }

    public Instant getExpirationAt() {
        return expirationAt;
    }
}
