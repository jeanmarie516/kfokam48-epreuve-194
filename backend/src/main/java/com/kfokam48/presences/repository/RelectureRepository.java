package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {

    boolean existsByExerciceId(Long exerciceId);

    List<Relecture> findByRelecteurId(Long relecteurId);

    List<Relecture> findByRelecteurIdAndRendueFalse(Long relecteurId);
}
