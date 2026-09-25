package com.kfokam48.presences.web;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.repository.ErreurSaisieCodeRepository;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.ExerciceRepository;
import com.kfokam48.presences.repository.PresenceRepository;
import com.kfokam48.presences.repository.PromotionRepository;
import com.kfokam48.presences.repository.RelectureRepository;
import com.kfokam48.presences.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * B6 — test d'intégration sur POST /api/presences, sur H2 en mémoire (aucune base locale requise).
 * Vérifie le contrat imposé : 201, 409 déjà présent, 410 code expiré, et le format d'erreur
 * { code, message } sans stack trace (B4).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PresencesApiTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    PromotionRepository promotions;
    @Autowired
    EtudiantRepository etudiants;
    @Autowired
    SessionRepository sessions;
    @Autowired
    PresenceRepository presences;
    @Autowired
    ExerciceRepository exercices;
    @Autowired
    RelectureRepository relectures;
    @Autowired
    ErreurSaisieCodeRepository erreursSaisie;

    /** Clock figé : permet de tester RG1 (expiration) sans attendre. */
    @MockBean
    Clock clock;

    private Instant maintenant = Instant.parse("2026-09-25T08:00:00Z");

    private Promotion promo;
    private Etudiant alice;
    private Etudiant bob;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.when(clock.instant()).thenAnswer(inv -> maintenant);
        org.mockito.Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        // Purge dans l'ordre des clés étrangères (les données de démo existent au démarrage).
        erreursSaisie.deleteAll();
        relectures.deleteAll();
        presences.deleteAll();
        exercices.deleteAll();
        sessions.deleteAll();
        etudiants.deleteAll();
        promotions.deleteAll();

        promo = promotions.save(new Promotion("K48-2026"));
        alice = etudiants.save(new Etudiant(promo, "Alice"));
        bob = etudiants.save(new Etudiant(promo, "Bob"));
    }

    private ResponseEntity<Map> post(String url, Map<String, Object> corps) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity(url, new HttpEntity<>(corps, headers), Map.class);
    }

    private Map<String, Object> ouvrirSession() {
        var reponse = post("/api/sessions", Map.of("titre", "Java 101", "promotionId", promo.getId()));
        assertThat(reponse.getStatusCode().value()).isEqualTo(201);
        return reponse.getBody();
    }

    @Test
    void presenceNominale201AvecSourceEtudiant() {
        Map<String, Object> session = ouvrirSession();

        var reponse = post("/api/presences",
                Map.of("code", session.get("code"), "etudiantId", alice.getId()));

        assertThat(reponse.getStatusCode().value()).isEqualTo(201);
        assertThat(reponse.getBody()).containsEntry("etudiantId", alice.getId().intValue());
        assertThat(reponse.getBody()).containsEntry("source", "ETUDIANT");
        assertThat(reponse.getBody()).containsKeys("id", "sessionId");
    }

    @Test
    void deuxiemePresenceRefusee409AvecFormatDErreurImposé() {
        Map<String, Object> session = ouvrirSession();
        post("/api/presences", Map.of("code", session.get("code"), "etudiantId", alice.getId()));

        var reponse = post("/api/presences",
                Map.of("code", session.get("code"), "etudiantId", alice.getId()));

        assertThat(reponse.getStatusCode().value()).isEqualTo(409); // RG2
        assertThat(reponse.getBody().get("code")).isEqualTo("DEJA_PRESENT");
        assertThat(reponse.getBody().get("message")).isInstanceOf(String.class);
    }

    @Test
    void codeExpireRefuse410() {
        Map<String, Object> session = ouvrirSession();

        maintenant = Instant.parse("2026-09-25T08:16:00Z"); // RG1 : +15 min dépassées

        var reponse = post("/api/presences",
                Map.of("code", session.get("code"), "etudiantId", bob.getId()));

        assertThat(reponse.getStatusCode().value()).isEqualTo(410);
        assertThat(reponse.getBody().get("code")).isEqualTo("CODE_EXPIRE");
        assertThat(reponse.getBody().get("message")).isEqualTo("Le code de présence a expiré.");
    }

    @Test
    void codeInconnuRefuse400AuFormatImposé() {
        var reponse = post("/api/presences", Map.of("code", "ZZZZZZ", "etudiantId", alice.getId()));

        assertThat(reponse.getStatusCode().value()).isEqualTo(400);
        assertThat(reponse.getBody().get("code")).isEqualTo("CODE_INCONNU");
    }

    @Test
    void promotionInconnue404SurLeTableau() {
        var reponse = rest.getForEntity("/api/tableau?promotionId=99999", Map.class);
        assertThat(reponse.getStatusCode().value()).isEqualTo(404);
        assertThat(reponse.getBody().get("code")).isEqualTo("PROMOTION_INCONNUE");
    }
}
