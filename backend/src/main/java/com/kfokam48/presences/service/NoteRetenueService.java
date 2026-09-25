package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.repository.RelectureRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * RG16/RG17/RG18 (v2 — enveloppe étape 3) :
 * - deux relectures rendues → note retenue = moyenne des deux, au dixième près (D3) ;
 * - une seule rendue → sa note, marquée provisoire ;
 * - aucune → pas de note (exercice en attente).
 * F3 : règle calculée ici uniquement — ni le tableau ni le frontend ne la dupliquent.
 */
@Service
public class NoteRetenueService {

    private final RelectureRepository relectures;

    public NoteRetenueService(RelectureRepository relectures) {
        this.relectures = relectures;
    }

    public record NoteRetenue(Double valeur, boolean provisoire, List<String> commentaires) {
    }

    public Optional<NoteRetenue> de(Exercice exercice) {
        List<Relecture> rendues = relectures.findByExerciceId(exercice.getId())
                .stream().filter(Relecture::estRendue).toList();
        if (rendues.isEmpty()) {
            return Optional.empty(); // RG18
        }
        if (rendues.size() == 1) {
            // RG17 : la note de l'unique relecteur qui a rendu, provisoire.
            return Optional.of(new NoteRetenue(rendues.get(0).getNote() * 1.0, true,
                    List.of(rendues.get(0).getCommentaire() == null ? "" : rendues.get(0).getCommentaire())));
        }
        // RG16 : moyenne des deux, au dixième près, sans arrondi (D3).
        double moyenne = Math.round(
                (rendues.get(0).getNote() + rendues.get(1).getNote()) / 2.0 * 10.0) / 10.0;
        return Optional.of(new NoteRetenue(moyenne, false,
                rendues.stream().map(r -> r.getCommentaire() == null ? "" : r.getCommentaire()).toList()));
    }

    /** Note retenue par étudiant pour le tableau : moyenne des notes retenues de ses exercices. */
    public Double moyenneEtudiant(List<Exercice> exercicesDeEtudiant) {
        List<Double> notes = exercicesDeEtudiant.stream()
                .map(this::de)
                .flatMap(Optional::stream)
                .map(NoteRetenue::valeur)
                .toList();
        if (notes.isEmpty()) {
            return null; // H5 : null si aucune note
        }
        return Math.round(notes.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 10.0) / 10.0;
    }
}
