package com.kfokam48.presences.dto;

import com.kfokam48.presences.domain.SessionCours;

import java.time.Instant;

/** { id, code, ouvertureAt, expirationAt } — format imposé par le contrat. */
public record SessionDto(Long id, String code, Instant ouvertureAt, Instant expirationAt, boolean cloturee) {

    public static SessionDto de(SessionCours s) {
        return new SessionDto(s.getId(), s.getCode(), s.getOuvertureAt(), s.getExpirationAt(), s.estCloturee());
    }
}
