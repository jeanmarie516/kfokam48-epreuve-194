package com.kfokam48.presences.service;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Exercice;
import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.domain.Relecture;
import com.kfokam48.presences.domain.SessionCours;
import com.kfokam48.presences.repository.RelectureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Enveloppe étape 3 — preuve des nouvelles règles :
 * RG16 (moyenne des deux relectures, au dixième, sans arrondi — D3),
 * RG17 (une seule rendue → provisoire), RG18 (aucune → pas de note).
 */
@ExtendWith(MockitoExtension.class)
class NoteRetenueServiceTest {

    @Mock
    private RelectureRepository relectures;

    private NoteRetenueService service;

    private Promotion promotion;
    private SessionCours session;
    private Exercice exercice;

    @BeforeEach
    void setUp() {
        service = new NoteRetenueService(relectures);
        promotion = new Promotion("K48");
        session = new SessionCours(promotion, "Java 101", "ABC123",
                Instant.parse("2026-09-25T08:00:00Z"), Instant.parse("2026-09-25T08:15:00Z"));
        exercice = new Exercice(session, new Etudiant(promotion, "Alice"),
                "https://github.com/alice/exo", Instant.now());
    }

    private Relecture relectureDe(String nom, Integer note) {
        Relecture r = new Relecture(exercice, new Etudiant(promotion, nom));
        if (note != null) {
            r.rendre(note, "commentaire de " + nom);
        }
        return r;
    }

    @Test
    void deuxRendues_moyenneAuDixiemeSansArrondi_RG16_D3() {
        // 14 et 15 → 14,5 : l'arrondi à l'entier déformerait le jugement des deux pairs.
        when(relectures.findByExerciceId(exercice.getId()))
                .thenReturn(List.of(relectureDe("Bob", 14), relectureDe("Chloé", 15)));

        Optional<NoteRetenueService.NoteRetenue> note = service.de(exercice);

        assertThat(note).isPresent();
        assertThat(note.get().valeur()).isEqualTo(14.5);
        assertThat(note.get().provisoire()).isFalse(); // définitive
        assertThat(note.get().commentaires()).hasSize(2);
    }

    @Test
    void uneSeuleRendue_noteProvisoire_RG17() {
        when(relectures.findByExerciceId(exercice.getId()))
                .thenReturn(List.of(relectureDe("Bob", 12), relectureDe("Chloé", null)));

        Optional<NoteRetenueService.NoteRetenue> note = service.de(exercice);

        assertThat(note).isPresent();
        assertThat(note.get().valeur()).isEqualTo(12.0);
        assertThat(note.get().provisoire()).isTrue();
    }

    @Test
    void aucuneRendue_pasDeNote_RG18() {
        when(relectures.findByExerciceId(exercice.getId()))
                .thenReturn(List.of(relectureDe("Bob", null), relectureDe("Chloé", null)));

        assertThat(service.de(exercice)).isEmpty();
    }
}
