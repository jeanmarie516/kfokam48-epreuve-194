package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.ErreurSaisieCode;
import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Presence;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.dto.PresenceDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.ErreurSaisieCodeRepository;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.PresenceRepository;
import com.kfokam48.presences.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * RG1 : code expiré après 15 min → 410.
 * RG2 : unicité de la présence → 409.
 * RG3 : session clôturée → 409.
 * RG13 : 5 erreurs consécutives de code → blocage 2 minutes → 429.
 * D2/H2 : une présence réussie retente le tirage des relecteurs en attente.
 */
@Service
public class PresenceService {

    private final PresenceRepository presences;
    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final ErreurSaisieCodeRepository erreurs;
    private final TirageRelecteurService tirageRelecteur;
    private final Clock clock;
    private final int erreursMax;
    private final int blocageMinutes;

    public PresenceService(PresenceRepository presences,
                           SessionRepository sessions,
                           EtudiantRepository etudiants,
                           ErreurSaisieCodeRepository erreurs,
                           TirageRelecteurService tirageRelecteur,
                           Clock clock,
                           @Value("${app.code-erreurs-max:5}") int erreursMax,
                           @Value("${app.blocage-minutes:2}") int blocageMinutes) {
        this.presences = presences;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.erreurs = erreurs;
        this.tirageRelecteur = tirageRelecteur;
        this.clock = clock;
        this.erreursMax = erreursMax;
        this.blocageMinutes = blocageMinutes;
    }

    @Transactional
    public PresenceDto marquer(String code, Long etudiantId) {
        if (code == null || code.isBlank()) {
            throw ApiException.champManquant("code");
        }
        if (etudiantId == null) {
            throw ApiException.champManquant("etudiantId");
        }
        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(ApiException::etudiantInconnu);

        // RG13 : un étudiant bloqué est refusé d'abord, même avec un code correct.
        verifierNonBloque(etudiantId);
        Instant maintenant = clock.instant();

        SessionCours session = sessions.findByCodeIgnoreCase(code.trim())
                .orElseThrow(ApiException::codeInconnu);

        // RG1 : un code connu mais expiré → 410.
        if (session.codeExpire(maintenant)) {
            throw ApiException.codeExpire();
        }

        // RG3 : plus de présence par code après clôture (Q3).
        if (session.estCloturee()) {
            throw ApiException.sessionCloturee();
        }

        // RG2 : unicité de la présence.
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiantId)) {
            throw ApiException.dejaPresent();
        }

        Presence presence = presences.save(Presence.parEtudiant(session, etudiant, maintenant));
        remettreCompteurAZero(etudiantId); // saisie réussie → compteur RG13 remis à zéro

        // D2/H2 : cette présence peut débloquer un tirage de relecteur en attente.
        tirageRelecteur.tirerPour(session);

        return PresenceDto.de(presence);
    }

    /**
     * Point d'entrée du contrôleur : si le code est inconnu, l'erreur compte pour RG13
     * (le code inconnu ne permet pas d'identifier une session, le comptage est par étudiant).
     */
    @Transactional
    public PresenceDto marquerAvecComptage(String code, Long etudiantId) {
        try {
            return marquer(code, etudiantId);
        } catch (ApiException e) {
            if ("CODE_INCONNU".equals(e.getCode()) && etudiantId != null
                    && etudiants.existsById(etudiantId)) {
                compterErreur(etudiantId);
            }
            throw e;
        }
    }

    private void verifierNonBloque(Long etudiantId) {
        erreurs.findByEtudiantId(etudiantId).ifPresent(c -> {
            if (c.getBloqueJusqua() != null && clock.instant().isBefore(c.getBloqueJusqua())) {
                throw ApiException.etudiantBloque();
            }
        });
    }

    private void compterErreur(Long etudiantId) {
        ErreurSaisieCode compteur = erreurs.findByEtudiantId(etudiantId)
                .orElseGet(() -> new ErreurSaisieCode(etudiantId));
        compteur.incrementer();
        if (compteur.getNbErreurs() >= erreursMax) { // RG13 : 5 erreurs → blocage
            compteur.setBloqueJusqua(clock.instant().plus(Duration.ofMinutes(blocageMinutes)));
        }
        erreurs.save(compteur);
    }

    private void remettreCompteurAZero(Long etudiantId) {
        erreurs.findByEtudiantId(etudiantId).ifPresent(c -> {
            c.reset();
            erreurs.save(c);
        });
    }
}
