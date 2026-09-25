package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.TableauLigneDto;
import com.kfokam48.presences.service.TableauService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * GET /api/tableau?promotionId= est imposé par le contrat :
 * 200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]
 * · 404 promotion inconnue.
 */
@RestController
@RequestMapping("/api/tableau")
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    @GetMapping
    public List<TableauLigneDto> voir(@RequestParam(required = false) Long promotionId) {
        if (promotionId == null) {
            throw com.kfokam48.presences.exception.ApiException.champManquant("promotionId");
        }
        return service.construire(promotionId);
    }
}
