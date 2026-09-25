package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.PresenceDto;
import com.kfokam48.presences.service.PresenceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * POST /api/presences est imposé par le contrat :
 * 201 { id, sessionId, etudiantId, source } · 400 code inconnu · 409 déjà présent · 410 code expiré.
 */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PresenceDto> marquer(@RequestBody(required = false) Map<String, Object> corps) {
        String code = corps == null ? null : (String) corps.get("code");
        Number etudiantId = corps == null ? null : (Number) corps.get("etudiantId");
        PresenceDto presence = service.marquerAvecComptage(code, etudiantId == null ? null : etudiantId.longValue());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(presence.id()).toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(presence);
    }
}
