package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.dto.RelectureAFaireDto;
import com.kfokam48.presences.dto.RelectureDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.RelectureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RG5 : interdiction de relire son propre exercice → 403.
 * RG9 : note entière 0–20 → 400 NOTE_INVALIDE sinon.
 * D1 : soumission unique (POST) + correction jusqu'à la clôture (PUT) — RG10, RG15.
 */
@Service
public class RelectureService {

    private final RelectureRepository relectures;

    public RelectureService(RelectureRepository relectures) {
        this.relectures = relectures;
    }

    @Transactional
    public RelectureDto rendre(Long relectureId, Long relecteurId, Number note, String commentaire) {
        Relecture r = charger(relectureId, relecteurId);
        if (r.estRendue()) {
            throw ApiException.relectureDejaRendue(); // 409 — soumission unique (D1)
        }
        int noteEntiere = validerNote(note); // RG9
        r.rendre(noteEntiere, commentaire);
        return RelectureDto.de(r);
    }

    @Transactional
    public RelectureDto corriger(Long relectureId, Long relecteurId, Number note, String commentaire) {
        Relecture r = charger(relectureId, relecteurId);
        SessionCours session = r.getExercice().getSession();
        if (session.estCloturee()) {
            throw ApiException.sessionCloturee(); // RG15 — la note est définitive
        }
        int noteEntiere = validerNote(note); // RG9
        r.corriger(noteEntiere, commentaire); // RG10/D1
        return RelectureDto.de(r);
    }

    @Transactional(readOnly = true)
    public List<RelectureAFaireDto> aFairePour(Long relecteurId) {
        return relectures.findByRelecteurId(relecteurId).stream()
                .map(RelectureAFaireDto::de)
                .toList();
    }

    private Relecture charger(Long relectureId, Long relecteurId) {
        if (relecteurId == null) {
            throw ApiException.champManquant("relecteurId");
        }
        Relecture r = relectures.findById(relectureId)
                .orElseThrow(ApiException::relectureInconnue);
        // RG5 d'abord : le déposant doit toujours recevoir 403, même s'il n'est pas
        // le relecteur assigné — c'est le code que le contrat attend de lui.
        if (r.getExercice().getDepositaire().getId().equals(relecteurId)) {
            throw ApiException.autoRelectureInterdite(); // RG5 → 403
        }
        if (!r.getRelecteur().getId().equals(relecteurId)) {
            throw ApiException.relectureInconnue(); // on ne révèle pas les relectures d'autrui
        }
        return r;
    }

    /** RG9 : la note doit être un entier entre 0 et 20. Un 12.5 ou un 25 est refusé → 400. */
    private int validerNote(Number note) {
        if (note == null) {
            throw ApiException.champManquant("note");
        }
        if (note.doubleValue() != Math.floor(note.doubleValue())) {
            throw ApiException.noteInvalide();
        }
        int valeur = note.intValue();
        if (valeur < 0 || valeur > 20) {
            throw ApiException.noteInvalide();
        }
        return valeur;
    }
}
