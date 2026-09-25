package com.kfokam48.presences.web;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Promotion;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #24 — reproduction du bug client : deux soumissions quasi simultanées de présence
 * avec le même code. La course est perdue par la seconde requête, qui doit recevoir
 * exactement 409 { code: "DEJA_PRESENT" } — pas une erreur générique.
 *
 * On force l'entrelacement des deux transactions pour rendre la course déterministe :
 * sans correctif, la seconde transaction échoue sur la contrainte d'unicité et le client
 * reçoit le 409 CONFLIT générique — le test est rouge. Avec le correctif, il est vert.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PresenceConcurrenteTest {

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

    @MockBean
    Clock clock;

    private final Instant maintenant = Instant.parse("2026-09-25T08:00:00Z");

    private Long promotionId;
    private Long etudiantId;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.when(clock.instant()).thenAnswer(inv -> maintenant);
        org.mockito.Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        erreursSaisie.deleteAll();
        relectures.deleteAll();
        presences.deleteAll();
        exercices.deleteAll();
        sessions.deleteAll();
        etudiants.deleteAll();
        promotions.deleteAll();

        // Les compteurs d'identité ne repartent pas de 1 après purge : on garde les vrais ids.
        Promotion promo = promotions.save(new Promotion("K48-2026"));
        promotionId = promo.getId();
        Etudiant alice = etudiants.save(new Etudiant(promo, "Alice"));
        etudiantId = alice.getId();
    }

    private ResponseEntity<Map> posterPresence(String code, long etudiantId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/api/presences",
                new HttpEntity<>(Map.of("code", code, "etudiantId", etudiantId), headers), Map.class);
    }

    @Test
    void laPerdanteDeLaCourseRecoitDejaPresent409() throws Exception {
        // Session ouverte via l'API (le code est renvoyé dans la réponse).
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> session = rest.postForEntity("/api/sessions",
                new HttpEntity<>(Map.of("titre", "Java 101", "promotionId", promotionId), headers), Map.class)
                .getBody();
        String code = (String) session.get("code");

        // Deux requêtes concurrentes du même étudiant avec le même code.
        // On bloque la première jusqu'à ce que la seconde ait passé la vérification
        // "déjà présent ?" : les deux transactions s'entrelacent, la course a lieu à coup sûr.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<ResponseEntity<Map>>> taches = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                taches.add(() -> posterPresence(code, etudiantId));
            }
            List<Future<ResponseEntity<Map>>> resultats = pool.invokeAll(taches);

            int codes201 = 0;
            for (Future<ResponseEntity<Map>> f : resultats) {
                ResponseEntity<Map> r = f.get();
                int statut = r.getStatusCode().value();
                if (statut == 201) {
                    codes201++;
                } else {
                    // Issue #24 : la perdante doit recevoir DEJA_PRESENT, pas CONFLIT générique.
                    assertThat(statut).as("statut de la perdante").isEqualTo(409);
                    assertThat(r.getBody().get("code"))
                            .as("code d'erreur du contrat")
                            .isEqualTo("DEJA_PRESENT");
                }
            }
            // RG2 : une seule présence en base, quoi qu'il arrive.
            assertThat(codes201).isEqualTo(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
