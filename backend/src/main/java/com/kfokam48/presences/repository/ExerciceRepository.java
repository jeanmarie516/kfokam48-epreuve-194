package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {

    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);

    List<Exercice> findBySessionId(Long sessionId);

    List<Exercice> findByDepositaireId(Long etudiantId);
}
