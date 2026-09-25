package com.kfokam48.presences.dto;

import java.util.List;

/**
 * { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente }
 * — format imposé par le contrat pour GET /api/tableau.
 */
public record TableauLigneDto(Long etudiantId, String nom, List<PresenceSessionDto> presences,
                              int exercicesDeposes, Double moyenne, int relecturesEnAttente) {

    public record PresenceSessionDto(Long sessionId, String source) {
    }
}
