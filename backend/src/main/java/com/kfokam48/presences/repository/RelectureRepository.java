package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    boolean existsByExerciceId(Long exerciceId);

    /** RG6 v2 : deux relectures par exercice → liste. */
    List<Relecture> findByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurId(Long relecteurId);

    List<Relecture> findByRelecteurIdAndRendueFalse(Long relecteurId);
}
