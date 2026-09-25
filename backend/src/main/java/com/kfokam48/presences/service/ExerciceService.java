package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.dto.ExerciceCreeDto;
import com.kfokam48.presences.dto.ExerciceDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.ExerciceRepository;
import com.kfokam48.presences.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Clock;
import java.util.List;

/**
 * RG12 : le dépôt reste possible après expiration du code, jusqu'à la clôture (Q12).
 * RG15 : une session clôturée refuse tout dépôt.
 * D2 : au dépôt, un relecteur est tiré au dépôt parmi les présents (H2 sinon).
 */
@Service
public class ExerciceService {

    private final ExerciceRepository exercices;
    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final TirageRelecteurService tirageRelecteur;
    private final Clock clock;

    public ExerciceService(ExerciceRepository exercices,
                           SessionRepository sessions,
                           EtudiantRepository etudiants,
                           TirageRelecteurService tirageRelecteur,
                           Clock clock) {
        this.exercices = exercices;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.tirageRelecteur = tirageRelecteur;
        this.clock = clock;
    }

    @Transactional
    public ExerciceCreeDto deposer(Long sessionId, Long etudiantId, String lien) {
        if (sessionId == null) {
            throw ApiException.champManquant("sessionId");
        }
        if (etudiantId == null) {
            throw ApiException.champManquant("etudiantId");
        }
        if (lien == null || lien.isBlank()) {
            throw ApiException.champManquant("lien");
        }
        validerLien(lien);

        SessionCours session = sessions.findById(sessionId)
                .orElseThrow(ApiException::sessionInconnue);
        if (session.estCloturee()) { // RG15
            throw ApiException.sessionCloturee();
        }
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(ApiException::etudiantInconnu);
        if (exercices.existsBySessionIdAndDepositaireId(sessionId, etudiantId)) {
            throw ApiException.exerciceDejaDepose(); // contrat : 409
        }

        Exercice exercice = exercices.save(new Exercice(session, etudiant, lien, clock.instant()));
        tirageRelecteur.tirerAuDepot(exercice); // D2 : tirage au dépôt, retenté aux présences suivantes
        return new ExerciceCreeDto(exercice.getId(), exercice.getStatut());
    }

    @Transactional
    public ExerciceDto remplacerLien(Long exerciceId, String lien) {
        if (lien == null || lien.isBlank()) {
            throw ApiException.champManquant("lien");
        }
        validerLien(lien);
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(ApiException::exerciceInconnu);
        exercice.remplacerLien(lien); // RG8 : lève LIEN_VERROUILLE si déjà relu
        return ExerciceDto.de(exercice);
    }

    @Transactional(readOnly = true)
    public List<ExerciceDto> listerPourSession(Long sessionId) {
        sessions.findById(sessionId).orElseThrow(ApiException::sessionInconnue);
        return exercices.findBySessionId(sessionId).stream().map(ExerciceDto::de).toList();
    }

    private void validerLien(String lien) { // contrat : 400 LIEN_INVALIDE
        try {
            URI uri = URI.create(lien.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme();
            if (!scheme.equals("http") && !scheme.equals("https") || uri.getHost() == null) {
                throw ApiException.lienInvalide();
            }
        } catch (IllegalArgumentException e) {
            throw ApiException.lienInvalide();
        }
    }
}
