package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Relecture;

/** Vue « relectures à faire » pour l'écran relecteur. */
public record RelectureAFaireDto(Long relectureId, Long exerciceId, String lien, boolean rendue,
                                 Integer note, String commentaire) {

    public static RelectureAFaireDto de(Relecture r) {
        return new RelectureAFaireDto(r.getId(), r.getExercice().getId(), r.getExercice().getLien(),
                r.estRendue(), r.getNote(), r.getCommentaire());
    }
}
