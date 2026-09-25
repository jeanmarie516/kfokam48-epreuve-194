package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.SessionDto;
import com.kfokam48.presences.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * POST /api/sessions est imposé par le contrat : 201 { id, code, ouvertureAt, expirationAt }.
 * Aucune logique métier ici — tout est délégué au service (B3).
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SessionDto> ouvrir(@RequestBody(required = false) Map<String, Object> corps) {
        String titre = corps == null ? null : (String) corps.get("titre");
        Number promotionId = corps == null ? null : (Number) corps.get("promotionId");
        SessionDto session = service.ouvrir(titre, promotionId == null ? null : promotionId.longValue());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(session.id()).toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(session);
    }

    @GetMapping("/{id}/code")
    public SessionDto voirCode(@PathVariable Long id) {
        return service.voirCode(id);
    }

    @PostMapping("/{id}/cloture")
    public ResponseEntity<Void> cloturer(@PathVariable Long id) {
        service.cloturer(id);
        return ResponseEntity.noContent().build();
    }
}
