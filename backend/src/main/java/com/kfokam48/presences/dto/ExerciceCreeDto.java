package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Exercice;

/** { id, statut } — format imposé par le contrat pour POST /api/exercices. */
public record ExerciceCreeDto(Long id, String statut) {
}
