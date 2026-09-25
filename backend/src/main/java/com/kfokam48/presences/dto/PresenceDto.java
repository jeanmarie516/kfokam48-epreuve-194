package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.Presence;

/** { id, sessionId, etudiantId, source } — format imposé par le contrat. */
public record PresenceDto(Long id, Long sessionId, Long etudiantId, String source) {

    public static PresenceDto de(Presence p) {
        return new PresenceDto(p.getId(), p.getSession().getId(), p.getEtudiant().getId(), p.getSource().name());
    }
}
