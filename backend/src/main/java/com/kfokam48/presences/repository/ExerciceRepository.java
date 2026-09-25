package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    /** Unicité (session, déposant) — le contrat impose 409 en cas de doublon. */
    boolean existsBySessionIdAndDepositaireId(Long sessionId, Long depositaireId);

    List<Exercice> findBySessionId(Long sessionId);

    List<Exercice> findByDepositaireId(Long depositaireId);
}
