package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.ErreurSaisieCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErreurSaisieCodeRepository extends JpaRepository<ErreurSaisieCode, Long> {

    Optional<ErreurSaisieCode> findByEtudiantId(Long etudiantId);
}
