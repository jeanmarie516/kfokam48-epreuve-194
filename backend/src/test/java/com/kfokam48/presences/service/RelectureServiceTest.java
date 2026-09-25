package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.dto.RelectureDto;
import com.kfokam48.presences.exception.ApiException;
import com.kfokam48.presences.repository.RelectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * B6 — test unitaire sur des règles métier réelles :
 * RG5 (jamais son propre exercice), RG9 (note entière 0–20), D1 (soumission unique).
 */
@ExtendWith(MockitoExtension.class)
class RelectureServiceTest {

    @Mock
    private RelectureRepository relectures;

    private RelectureService service;

    private SessionCours session;
    private Etudiant depositaire;
    private Etudiant relecteur;
    private Exercice exercice;
    private Relecture relecture;

    @BeforeEach
    void setUp() {
        service = new RelectureService(relectures);

        var promotion = new Promotion("K48");
        depositaire = new Etudiant(promotion, "Alice");
        relecteur = new Etudiant(promotion, "Bob");
        session = new SessionCours(promotion, "Java 101", "ABC123",
                Instant.parse("2026-09-25T08:00:00Z"), Instant.parse("2026-09-25T08:15:00Z"));
        exercice = new Exercice(session, depositaire, "https://github.com/alice/exo", Instant.now());
        relecture = new Relecture(exercice, relecteur);
    }

    private void setId(Object entite, Long id) throws Exception {
        var champ = entite.getClass().getSuperclass().equals(Object.class)
                ? entite.getClass().getDeclaredField("id")
                : entite.getClass().getDeclaredField("id");
        champ.setAccessible(true);
        champ.set(entite, id);
    }

    @Test
    void unRelecteurNePeutPasRelireSonPropreExercice_RG5() throws Exception {
        setId(exercice, 1L);
        setId(depositaire, 10L);
        setId(relecteur, 20L);
        setId(relecture, 100L);
        when(relectures.findById(100L)).thenReturn(Optional.of(relecture));

        // Le déposant tente de relire son propre exercice : c'est le principe même (Q5).
        assertThatThrownBy(() -> service.rendre(100L, 10L, 15, "bravo"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("AUTO_RELECTURE_INTERDITE"))
                .satisfies(e -> assertThat(((ApiException) e).getStatus().value()).isEqualTo(403));
    }

    @Test
    void uneNoteNonEntiereEstRefusee_RG9() throws Exception {
        setId(exercice, 1L);
        setId(depositaire, 10L);
        setId(relecteur, 20L);
        setId(relecture, 100L);
        when(relectures.findById(100L)).thenReturn(Optional.of(relecture));

        assertThatThrownBy(() -> service.rendre(100L, 20L, 12.5, "presque"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("NOTE_INVALIDE"))
                .satisfies(e -> assertThat(((ApiException) e).getStatus().value()).isEqualTo(400));

        assertThatThrownBy(() -> service.rendre(100L, 20L, 25, "trop"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("NOTE_INVALIDE"));
    }

    @Test
    void uneRelectureDejaRendueEstRefusee_409() throws Exception {
        setId(exercice, 1L);
        setId(depositaire, 10L);
        setId(relecteur, 20L);
        setId(relecture, 100L);
        relecture.rendre(14, "bien");
        when(relectures.findById(100L)).thenReturn(Optional.of(relecture));

        assertThatThrownBy(() -> service.rendre(100L, 20L, 15, "je change d'avis"))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo("RELECTURE_DEJA_RENDUE"))
                .satisfies(e -> assertThat(((ApiException) e).getStatus().value()).isEqualTo(409));
    }

    @Test
    void laCorrectionAvantClotureModifieLaNote_RG10() throws Exception {
        setId(exercice, 1L);
        setId(depositaire, 10L);
        setId(relecteur, 20L);
        setId(relecture, 100L);
        relecture.rendre(14, "bien");
        when(relectures.findById(100L)).thenReturn(Optional.of(relecture));

        RelectureDto dto = service.corriger(100L, 20L, 16, "en fait c'est très bien");

        assertThat(dto.note()).isEqualTo(16);
        assertThat(relecture.getNote()).isEqualTo(16);
    }
}
