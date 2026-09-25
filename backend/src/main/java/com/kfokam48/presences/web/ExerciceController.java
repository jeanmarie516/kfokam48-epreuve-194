package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.ExerciceCreeDto;
import com.kfokam48.presences.dto.ExerciceDto;
import com.kfokam48.presences.service.ExerciceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * POST /api/exercices est imposé par le contrat :
 * 201 { id, statut } · 400 lien invalide · 409 exercice déjà déposé.
 */
@RestController
@RequestMapping("/api")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @PostMapping("/exercices")
    public ResponseEntity<ExerciceCreeDto> deposer(@RequestBody(required = false) Map<String, Object> corps) {
        Number sessionId = corps == null ? null : (Number) corps.get("sessionId");
        Number etudiantId = corps == null ? null : (Number) corps.get("etudiantId");
        String lien = corps == null ? null : (String) corps.get("lien");
        ExerciceCreeDto exercice = service.deposer(
                sessionId == null ? null : sessionId.longValue(),
                etudiantId == null ? null : etudiantId.longValue(),
                lien);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/exercices/{id}").buildAndExpand(exercice.id()).toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(exercice);
    }

    @PutMapping("/exercices/{id}/lien")
    public ExerciceDto remplacerLien(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, Object> corps) {
        String lien = corps == null ? null : (String) corps.get("lien");
        return service.remplacerLien(id, lien);
    }

    @GetMapping("/sessions/{id}/exercices")
    public List<ExerciceDto> lister(@PathVariable Long id) {
        return service.listerPourSession(id);
    }
}
