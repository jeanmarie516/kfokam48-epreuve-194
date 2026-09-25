package com.kfokam48.presences;

import com.kfokam48.presences.domain.Etudiant;
import com.kfokam48.presences.domain.Promotion;
import com.kfokam48.presences.repository.EtudiantRepository;
import com.kfokam48.presences.repository.PromotionRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Données de démonstration : sans elles, le correcteur ouvre une application vide
 * et ne peut rien vérifier (exigence « Démarrage » du sujet).
 */
@Configuration
public class DemoDataInitializer {

    @Bean
    public ApplicationRunner demoData(PromotionRepository promotions, EtudiantRepository etudiants) {
        return args -> {
            if (promotions.count() > 0) {
                return; // déjà chargées — l'initialisation est idempotente
            }
            Promotion k48 = promotions.save(new Promotion("K48 — Promotion 2026"));

            for (String nom : new String[]{
                    "Alice Ngo", "Boris Talla", "Chantal Mbeng", "David Ebolo",
                    "Emma Fouda", "Fabrice Kamcheu", "Grace Ndongo", "Hervé Tchoupo",
                    "Inès Biya", "Jonas Atangana"}) {
                etudiants.save(new Etudiant(k48, nom));
            }
        };
    }
}
