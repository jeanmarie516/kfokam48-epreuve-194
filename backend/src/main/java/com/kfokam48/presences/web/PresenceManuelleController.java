package com.kfokam48.presences.web;

import com.kfokam48.presences.domain.Presence;
import com.kfokam48.presences.dto.PresenceDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.PresenceRepository;
import com.kfokam48.presences.repository.SessionRepository;
import com.kfokam48.presences.service.TirageRelecteurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Clock;
import java.util.Map;

/**
 * Complément du contrat : POST /api/sessions/{id}/presences — ajout manuel par le formateur.
 * RG4 : la présence porte source=FORMATEUR (Q14). RG2 : l'unicité s'applique aussi ici.
 */
@RestController
public class PresenceManuelleController {

    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final TirageRelecteurService tirageRelecteur;
    private final Clock clock;

    public PresenceManuelleController(SessionRepository sessions,
                                      EtudiantRepository etudiants,
                                      PresenceRepository presences,
                                      TirageRelecteurService tirageRelecteur,
                                      Clock clock) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.tirageRelecteur = tirageRelecteur;
        this.clock = clock;
    }

    @PostMapping("/api/sessions/{id}/presences")
    public ResponseEntity<PresenceDto> ajouter(@PathVariable Long id,
                                               @RequestBody(required = false) Map<String, Object> corps) {
        Number etudiantId = corps == null ? null : (Number) corps.get("etudiantId");
        if (etudiantId == null) {
            throw ApiException.champManquant("etudiantId");
        }
        var session = sessions.findById(id).orElseThrow(ApiException::sessionInconnue);
        var etudiant = etudiants.findById(etudiantId.longValue()).orElseThrow(ApiException::etudiantInconnu);
        if (presences.existsBySessionIdAndEtudiantId(id, etudiant.getId())) {
            throw ApiException.dejaPresent(); // RG2
        }
        Presence presence = presences.save(Presence.parFormateur(session, etudiant, clock.instant())); // RG4
        tirageRelecteur.tirerPour(session); // une présence manuelle compte aussi (RG7)
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(presence.getId()).toUri();
        return ResponseEntity.status(HttpStatus.CREATED).body(PresenceDto.de(presence));
    }
}
