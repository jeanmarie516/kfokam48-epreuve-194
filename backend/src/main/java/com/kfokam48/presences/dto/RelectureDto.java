package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Relecture;

/** { id, exerciceId, note, commentaire, rendue }. */
public record RelectureDto(Long id, Long exerciceId, Integer note, String commentaire, boolean rendue) {

    public static RelectureDto de(Relecture r) {
        return new RelectureDto(r.getId(), r.getExercice().getId(), r.getNote(), r.getCommentaire(), r.estRendue());
    }
}
