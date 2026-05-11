package com.eduplatform.repository;

import com.eduplatform.model.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Long> {

    // Returns list — safe even if duplicates exist in DB
    List<UserProgressEntity> findAllByUsername(String username);

    // Delete duplicates keeping only the one with lowest ID
    @Modifying
    @Transactional
    @Query("DELETE FROM UserProgressEntity u WHERE u.username = :username AND u.id > (SELECT MIN(u2.id) FROM UserProgressEntity u2 WHERE u2.username = :username)")
    void deleteDuplicatesByUsername(@Param("username") String username);
}
