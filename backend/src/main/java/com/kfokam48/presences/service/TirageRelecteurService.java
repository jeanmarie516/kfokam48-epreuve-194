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
import java.util.concurrent.ThreadLocalRandom;

/**
 * RG7 : les relecteurs sont choisis par le système, au hasard, parmi les étudiants présents à la session.
 * RG5 : jamais le déposant lui-même.
 * RG6 v2 (enveloppe étape 3) : exactement deux relecteurs distincts par exercice.
 * D2/H2 : le tirage a lieu au dépôt ; s'il y a moins de deux candidats, il est complété
 * à chaque nouvelle présence de la session.
 */
@Service
public class TirageRelecteurService {

    /** RG6 v2 : deux relecteurs par exercice. */
    public static final int NB_RELECTEURS = 2;

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

    /** Au dépôt d'un exercice : tente d'assigner immédiatement jusqu'à deux relecteurs. */
    @Transactional
    public void tirerAuDepot(Exercice exercice) {
        completerAssignation(exercice);
    }

    /** À chaque nouvelle présence : complète le tirage des exercices qui n'ont pas encore deux relecteurs. */
    @Transactional
    public void tirerPour(SessionCours session) {
        List<Exercice> incomplets = exercices.findBySessionId(session.getId()).stream()
                .filter(e -> relectures.findByExerciceId(e.getId()).size() < NB_RELECTEURS)
                .toList();
        for (Exercice e : incomplets) {
            completerAssignation(e);
        }
    }

    private void completerAssignation(Exercice exercice) {
        List<Etudiant> presents = presences.findBySessionId(exercice.getSession().getId()).stream()
                .map(Presence::getEtudiant)
                .filter(e -> !e.getId().equals(exercice.getDepositaire().getId())) // RG5
                .toList();
        while (relectures.findByExerciceId(exercice.getId()).size() < NB_RELECTEURS
                && !presents.isEmpty()) {
            List<Long> dejaAssignes = relectures.findByExerciceId(exercice.getId()).stream()
                    .map(r -> r.getRelecteur().getId())
                    .toList();
            List<Etudiant> candidats = presents.stream()
                    .filter(e -> !dejaAssignes.contains(e.getId())) // distincts, RG6 v2
                    .toList();
            if (candidats.isEmpty()) {
                return; // H2 : tous les présents sont déjà assignés, on complètera à la prochaine présence
            }
            Etudiant elu = candidats.get(ThreadLocalRandom.current().nextInt(candidats.size())); // RG7
            relectures.save(new Relecture(exercice, elu));
        }
    }
}
