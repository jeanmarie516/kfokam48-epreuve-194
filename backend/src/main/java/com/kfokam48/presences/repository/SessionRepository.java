package com.kfokam48.presences.repository;

import com.kfokam48.presences.domain.SessionCours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionCours, Long> {

    boolean existsByCode(String code);

    java.util.Optional<SessionCours> findByCodeIgnoreCase(String code);

    java.util.List<SessionCours> findByPromotionIdOrderByOuvertureAt(Long promotionId);
}
