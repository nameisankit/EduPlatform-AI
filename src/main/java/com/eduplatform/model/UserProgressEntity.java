package com.eduplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

/**
 * User progress entity - tracks streaks, total points, badges
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_progress")
public class UserProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private int totalPoints;
    private int totalSessions;
    private int totalCorrectAnswers;
    private int totalQuestions;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActiveDate;

    @Column(length = 5000)
    private String allBadgesJson;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
