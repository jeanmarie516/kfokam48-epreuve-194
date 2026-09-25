package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Exercice;

/**
 * Vue d'un exercice. relectureRendue permet au frontend de savoir si le lien
 * est verrouillé (RG8) sans recalculer de règle métier (F3).
 */
public record ExerciceDto(Long id, Long sessionId, Long etudiantId, String lien, String statut, boolean relectureRendue) {

    public static ExerciceDto de(Exercice e) {
        return new ExerciceDto(e.getId(), e.getSession().getId(), e.getDepositaire().getId(),
                e.getLien(), e.getStatut(), Exercice.RELU.equals(e.getStatut()));
    }
}
