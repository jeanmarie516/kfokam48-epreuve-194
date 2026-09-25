package com.kfokam48.presences.service;

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
 * v2 (enveloppe) : la moyenne porte sur les notes retenues (RG16), et les relectures
 * en attente concernent les deux relecteurs (RG18). F3 : calcul côté API uniquement.
 */
@Service
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final SessionRepository sessions;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final NoteRetenueService noteRetenue;

    public TableauService(PromotionRepository promotions,
                          EtudiantRepository etudiants,
                          SessionRepository sessions,
                          PresenceRepository presences,
                          ExerciceRepository exercices,
                          RelectureRepository relectures,
                          NoteRetenueService noteRetenue) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.sessions = sessions;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
        this.noteRetenue = noteRetenue;
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

                    var exos = exercices.findByDepositaireId(e.getId());
                    int nbExercices = exos.size();

                    // RG16 : moyenne des notes retenues (moyenne des deux relectures par exercice).
                    Double moyenne = noteRetenue.moyenneEtudiant(exos);

                    int relecturesEnAttente = (int) relectures.findByRelecteurId(e.getId()).stream()
                            .filter(r -> !r.estRendue())
                            .count();

                    return new TableauLigneDto(e.getId(), e.getNom(), presencesSession,
                            nbExercices, moyenne, relecturesEnAttente);
                })
                .toList();
    }
}
