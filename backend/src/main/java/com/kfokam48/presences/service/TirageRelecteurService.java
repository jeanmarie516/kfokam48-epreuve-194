package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.Presence;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.ExerciceRepository;
import com.kfokam48.presences.repository.PresenceRepository;
import com.kfokam48.presences.repository.RelectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RG7 : le relecteur est choisi par le système, au hasard, parmi les étudiants présents à la session.
 * RG5 : jamais le déposant lui-même.
 * RG6 : un seul relecteur par exercice — une relecture est créée à l'assignation, non rendue.
 * D2/H2 : le tirage a lieu au dépôt ; si aucun autre étudiant n'est présent, il est retenté
 * à chaque nouvelle présence de la session.
 */
@Service
public class TirageRelecteurService {

    private final ExerciceRepository exercices;
    private final PresenceRepository presences;
    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;

    public TirageRelecteurService(ExerciceRepository exercices,
                                  PresenceRepository presences,
                                  RelectureRepository relectures,
                                  EtudiantRepository etudiants) {
        this.exercices = exercices;
        this.presences = presences;
        this.relectures = relectures;
        this.etudiants = etudiants;
    }

    /** Au dépôt d'un exercice : tente d'assigner un relecteur immédiatement. */
    @Transactional
    public void tirerAuDepot(Exercice exercice) {
        assignerSiNecessaire(exercice);
    }

    /** À chaque nouvelle présence : retente le tirage pour les exercices de la session sans relecteur. */
    @Transactional
    public void tirerPour(SessionCours session) {
        List<Exercice> sansRelecture = exercices.findBySessionId(session.getId()).stream()
                .filter(e -> e.getStatut().equals(Exercice.EN_ATTENTE))
                .filter(e -> !relectures.existsByExerciceId(e.getId()))
                .toList();
        for (Exercice e : sansRelecture) {
            assignerSiNecessaire(e);
        }
    }

    private void assignerSiNecessaire(Exercice exercice) {
        if (relectures.existsByExerciceId(exercice.getId())) {
            return; // RG6 : un seul relecteur, déjà assigné
        }
        List<Etudiant> presents = presences.findBySessionId(exercice.getSession().getId()).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !e.getId().equals(exercice.getDepositaire().getId())) // RG5
                .toList();
        if (presents.isEmpty()) {
            return; // H2 : pas de candidat, on retentera à la prochaine présence
        }
        Etudiant elu = presents.get(ThreadLocalRandom.current().nextInt(presents.size())); // RG7
        relectures.save(new Relecture(exercice, elu));
    }
}
