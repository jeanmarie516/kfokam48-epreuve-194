package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.RelectureAFaireDto;
import com.kfokam48.presences.dto.RelectureDto;
import com.kfokam48.presences.service.RelectureService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * POST /api/relectures/{id} est imposé par le contrat :
 * 200 · 400 note hors 0–20 ou non entière · 403 relecture de son propre exercice ·
 * 409 relecture déjà rendue.
 * PUT réalise la correction avant clôture (Q10 tranché en D1).
 */
@RestController
@RequestMapping("/api/relectures")
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    @PostMapping("/{id}")
    public RelectureDto rendre(@PathVariable Long id,
                               @RequestBody(required = false) Map<String, Object> corps) {
        Number relecteurId = corps == null ? null : (Number) corps.get("relecteurId");
        Number note = corps == null ? null : (Number) corps.get("note");
        String commentaire = corps == null ? null : (String) corps.get("commentaire");
        return service.rendre(id, relecteurId == null ? null : relecteurId.longValue(), note, commentaire);
    }

    @PutMapping("/{id}")
    public RelectureDto corriger(@PathVariable Long id,
                                 @RequestBody(required = false) Map<String, Object> corps) {
        Number relecteurId = corps == null ? null : (Number) corps.get("relecteurId");
        Number note = corps == null ? null : (Number) corps.get("note");
        String commentaire = corps == null ? null : (String) corps.get("commentaire");
        return service.corriger(id, relecteurId == null ? null : relecteurId.longValue(), note, commentaire);
    }

    @GetMapping("/a-faire")
    public List<RelectureAFaireDto> aFaire(@RequestParam Long relecteurId) {
        return service.aFairePour(relecteurId);
    }
}
