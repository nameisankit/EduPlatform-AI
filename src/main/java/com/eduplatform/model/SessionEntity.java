package com.eduplatform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Persistent learning session entity stored in H2 DB
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String level;
    private String language;

    @Column(length = 10000)
    private String curriculum;

    @Column(length = 20000)
    private String lesson;

    @Column(length = 10000)
    private String quizJson;

    @Column(length = 10000)
    private String feedback;

    @Column(length = 5000)
    private String imagePrompt;

    @Column(length = 500000)
    private String imageUrl;

    @Column(length = 10000)
    private String structuredContentJson;

    @Column(length = 20000)
    private String chatHistoryJson;

    @Column(length = 5000)
    private String bookmarksJson;

    @Column(length = 5000)
    private String summary;

    @Column(length = 5000)
    private String relatedTopicsJson;

    // Gamification
    private int points;
    private int correctAnswers;
    private int totalQuestions;
    private String earnedBadgesJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
