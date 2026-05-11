package com.eduplatform.repository;

import com.eduplatform.model.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Long> {
    Optional<UserProgressEntity> findByUsername(String username);
}
