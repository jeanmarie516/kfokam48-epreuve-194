package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.domain.SessionCours;

/**
 * DTO du référentiel pour les sélecteurs d'identité des écrans (Q1 :
 * l'étudiant choisit son nom dans une liste).
 */
public final class ReferentielDtos {

    private ReferentielDtos() {
    }

    public record PromotionDto(Long id, String nom) {
        public static PromotionDto de(Promotion p) {
            return new PromotionDto(p.getId(), p.getNom());
        }
    }

    public record EtudiantDto(Long id, String nom) {
        public static EtudiantDto de(Etudiant e) {
            return new EtudiantDto(e.getId(), e.getNom());
        }
    }

    public record SessionResumeDto(Long id, String titre, boolean cloturee,
                                   java.time.Instant ouvertureAt, java.time.Instant expirationAt) {
        public static SessionResumeDto de(SessionCours s) {
            return new SessionResumeDto(s.getId(), s.getTitre(), s.estCloturee(),
                    s.getOuvertureAt(), s.getExpirationAt());
        }
    }

    /** Vue « étudiant relu » : note et commentaire oui, nom du relecteur jamais (RG14, Q8). */
    public record ExerciceEtudiantDto(Long id, Long sessionId, String lien, String statut, RelectureRecueDto relectureRecue) {

        public record RelectureRecueDto(Integer note, String commentaire) {
        }

        public static ExerciceEtudiantDto de(Exercice e, Relecture r) {
            RelectureRecueDto recue = (r != null && r.estRendue())
                    ? new RelectureRecueDto(r.getNote(), r.getCommentaire())
                    : null;
            return new ExerciceEtudiantDto(e.getId(), e.getSession().getId(), e.getLien(), e.getStatut(), recue);
        }
    }
}
