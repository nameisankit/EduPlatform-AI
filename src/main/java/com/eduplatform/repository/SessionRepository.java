package com.eduplatform.repository;

import com.eduplatform.model.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
    List<SessionEntity> findAllByOrderByCreatedAtDesc();
    List<SessionEntity> findByTopicOrderByCreatedAtDesc(String topic);
}
