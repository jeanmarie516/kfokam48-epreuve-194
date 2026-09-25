package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.dto.SessionDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.PromotionRepository;
import com.kfokam48.presences.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class SessionService {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final Clock clock;
    private final int expirationMinutes;
    private final int codeLongueur;
    private final SecureRandom random = new SecureRandom();

    public SessionService(SessionRepository sessions,
                          PromotionRepository promotions,
                          Clock clock,
                          @Value("${app.code-expiration-minutes:15}") int expirationMinutes,
                          @Value("${app.code-longueur:6}") int codeLongueur) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.clock = clock;
        this.expirationMinutes = expirationMinutes;
        this.codeLongueur = codeLongueur;
    }

    @Transactional
    public SessionDto ouvrir(String titre, Long promotionId) {
        if (titre == null || titre.isBlank()) {
            throw ApiException.champManquant("titre");
        }
        if (promotionId == null) {
            throw ApiException.champManquant("promotionId");
        }
        Promotion promotion = promotions.findById(promotionId)
                .orElseThrow(ApiException::promotionInconnue);

        Instant maintenant = clock.instant();
        String code = genererCodeUnique();
        SessionCours session = new SessionCours(
                promotion, titre, code, maintenant,
                maintenant.plus(Duration.ofMinutes(expirationMinutes))); // RG1
        sessions.save(session);
        return SessionDto.de(session);
    }

    @Transactional(readOnly = true)
    public SessionDto voirCode(Long id) {
        SessionCours session = sessions.findById(id).orElseThrow(ApiException::sessionInconnue);
        return SessionDto.de(session);
    }

    @Transactional
    public void cloturer(Long id) {
        SessionCours session = sessions.findById(id).orElseThrow(ApiException::sessionInconnue);
        session.cloturer(); // RG15
    }

    private String genererCodeUnique() {
        StringBuilder sb = new StringBuilder(codeLongueur);
        for (int i = 0; i < codeLongueur; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        String code = sb.toString();
        if (sessions.existsByCode(code)) {
            return genererCodeUnique();
        }
        return code;
    }
}
