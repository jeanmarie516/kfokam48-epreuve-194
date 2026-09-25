package com.kfokam48.presences.web;

import com.kfokam48.presences.dto.ReferentielDtos;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.ExerciceRepository;
import com.kfokam48.presences.repository.PromotionRepository;
import com.kfokam48.presences.repository.RelectureRepository;
import com.kfokam48.presences.repository.SessionRepository;
import com.kfokam48.presences.service.NoteRetenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Compléments du contrat : référentiels qui alimentent les sélecteurs d'identité (Q1)
 * et la vue « étudiant relu » (RG14 : le nom du relecteur n'est jamais exposé).
 */
@RestController
@RequestMapping("/api")
public class ReferentielController {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final SessionRepository sessions;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final NoteRetenueService noteRetenue;

    public ReferentielController(PromotionRepository promotions,
                                 EtudiantRepository etudiants,
                                 SessionRepository sessions,
                                 ExerciceRepository exercices,
                                 RelectureRepository relectures,
                                 NoteRetenueService noteRetenue) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.sessions = sessions;
        this.exercices = exercices;
        this.relectures = relectures;
        this.noteRetenue = noteRetenue;
    }

    @GetMapping("/promotions")
    public List<ReferentielDtos.PromotionDto> promotions() {
        return promotions.findAll().stream().map(ReferentielDtos.PromotionDto::de).toList();
    }

    @GetMapping("/promotions/{id}/etudiants")
    public List<ReferentielDtos.EtudiantDto> etudiants(@PathVariable Long id) {
        promotions.findById(id).orElseThrow(ApiException::promotionInconnue);
        return etudiants.findByPromotionIdOrderByNom(id).stream()
                .map(ReferentielDtos.EtudiantDto::de).toList();
    }

    @GetMapping("/promotions/{id}/sessions")
    public List<ReferentielDtos.SessionResumeDto> sessions(@PathVariable Long id) {
        promotions.findById(id).orElseThrow(ApiException::promotionInconnue);
        return sessions.findByPromotionIdOrderByOuvertureAt(id).stream()
                .map(ReferentielDtos.SessionResumeDto::de).toList();
    }

    @GetMapping("/promotions/{id}/etudiants/{etudiantId}/exercices")
    public List<ReferentielDtos.ExerciceEtudiantDto> exercicesEtudiant(@PathVariable Long id,
                                                                       @PathVariable Long etudiantId) {
        promotions.findById(id).orElseThrow(ApiException::promotionInconnue);
        var etudiant = etudiants.findById(etudiantId).orElseThrow(ApiException::etudiantInconnu);
        return exercices.findByDepositaireId(etudiant.getId()).stream()
                .map(e -> ReferentielDtos.ExerciceEtudiantDto.de(
                        e,
                        noteRetenue.de(e)
                                .map(n -> new ReferentielDtos.ExerciceEtudiantDto.NoteRetenueDto(
                                        n.valeur(), n.provisoire(), n.commentaires()))
                                .orElse(null)))
                .toList();
    }
}
