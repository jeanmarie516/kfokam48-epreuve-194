package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Presence;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.dto.TableauLigneDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * GET /api/tableau?promotionId= — Q16 : par étudiant, sa présence à chaque session,
 * son nombre d'exercices déposés, la moyenne des notes reçues, et ses relectures en attente.
 * F3 : la moyenne est calculée ici, le frontend ne la recalcule jamais.
 */
@Service
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final SessionRepository sessions;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(PromotionRepository promotions,
                          EtudiantRepository etudiants,
                          SessionRepository sessions,
                          PresenceRepository presences,
                          ExerciceRepository exercices,
                          RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.sessions = sessions;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    @Transactional(readOnly = true)
    public List<TableauLigneDto> construire(Long promotionId) {
        promotions.findById(promotionId).orElseThrow(ApiException::promotionInconnue); // contrat : 404

        return etudiants.findByPromotionIdOrderByNom(promotionId).stream()
                .map(e -> {
                    List<TableauLigneDto.PresenceSessionDto> presencesSession =
                            sessions.findByPromotionIdOrderByOuvertureAt(promotionId).stream()
                                    .map(s -> presences.findBySessionId(s.getId()).stream()
                                            .filter(p -> p.getEtudiant().getId().equals(e.getId()))
                                            .findFirst()
                                            .map(p -> new TableauLigneDto.PresenceSessionDto(
                                                    s.getId(), p.getSource().name()))
                                            .orElse(null))
                                    .toList();

                    int nbExercices = exercices.findByDepositaireId(e.getId()).size();

                    // Moyenne des notes reçues par l'étudiant (ses exercices relu par les pairs).
                    List<Integer> notes = exercices.findByDepositaireId(e.getId()).stream()
                            .map(ex -> relectures.findByExerciceId(ex.getId()).orElse(null))
                            .filter(r -> r != null && r.estRendue())
                            .map(Relecture::getNote)
                            .toList();
                    Double moyenne = notes.isEmpty() ? null : // H5 : null si aucune note
                            Math.round(notes.stream().mapToInt(Integer::intValue).average().orElse(0))
                                    * 1.0;

                    int relecturesEnAttente = (int) relectures.findByRelecteurId(e.getId()).stream()
                            .filter(r -> !r.estRendue())
                            .count();

                    return new TableauLigneDto(e.getId(), e.getNom(), presencesSession,
                            nbExercices, moyenne, relecturesEnAttente);
                })
                .toList();
    }
}
