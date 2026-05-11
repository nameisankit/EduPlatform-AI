package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import com.eduplatform.model.SessionEntity;
import com.eduplatform.model.UserProgressEntity;
import com.eduplatform.repository.SessionRepository;
import com.eduplatform.repository.UserProgressRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles persistent storage of learning sessions and gamification
 */
@Service
public class SessionPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(SessionPersistenceService.class);
    private static final String DEFAULT_USER = "default";

    private final SessionRepository sessionRepository;
    private final UserProgressRepository userProgressRepository;
    private final ObjectMapper objectMapper;

    public SessionPersistenceService(SessionRepository sessionRepository,
                                     UserProgressRepository userProgressRepository,
                                     ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.userProgressRepository = userProgressRepository;
        this.objectMapper = objectMapper;
    }

    // ─── Save session to DB ─────────────────────────────────────────────────
    public SessionEntity saveSession(LearningMemory memory, int correctAnswers, int totalQuestions) {
        try {
            SessionEntity entity = SessionEntity.builder()
                    .topic(memory.getTopic())
                    .level(memory.getLevel())
                    .language(memory.getLanguage() != null ? memory.getLanguage() : "English")
                    .curriculum(memory.getCurriculum())
                    .lesson(memory.getLesson())
                    .quizJson(toJson(memory.getQuiz()))
                    .feedback(memory.getFeedback())
                    .imagePrompt(memory.getImagePrompt())
                    .imageUrl(memory.getImageUrl())
                    .structuredContentJson(toJson(memory.getStructuredContent()))
                    .chatHistoryJson(toJson(memory.getChatHistory()))
                    .bookmarksJson(toJson(memory.getBookmarks()))
                    .summary(memory.getSummary())
                    .relatedTopicsJson(toJson(memory.getRelatedTopics()))
                    .points(calculatePoints(correctAnswers, totalQuestions))
                    .correctAnswers(correctAnswers)
                    .totalQuestions(totalQuestions)
                    .earnedBadgesJson(toJson(memory.getEarnedBadges()))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            entity = sessionRepository.save(entity);

            // Update user progress
            updateUserProgress(entity.getPoints(), correctAnswers, totalQuestions);

            return entity;
        } catch (Exception e) {
            log.error("Failed to save session: {}", e.getMessage(), e);
            return null;
        }
    }

    // ─── Get all sessions for dashboard ──────────────────────────────────────
    public List<SessionEntity> getAllSessions() {
        return sessionRepository.findAllByOrderByCreatedAtDesc();
    }

    // ─── Get session by ID ───────────────────────────────────────────────────
    public Optional<SessionEntity> getSession(Long id) {
        return sessionRepository.findById(id);
    }

    // ─── Convert session entity back to LearningMemory ───────────────────────
    public LearningMemory toMemory(SessionEntity entity) {
        LearningMemory memory = LearningMemory.builder()
                .topic(entity.getTopic())
                .level(entity.getLevel())
                .language(entity.getLanguage())
                .curriculum(entity.getCurriculum())
                .lesson(entity.getLesson())
                .quiz(fromJson(entity.getQuizJson(), LearningMemory.QuizData.class))
                .feedback(entity.getFeedback())
                .imagePrompt(entity.getImagePrompt())
                .imageUrl(entity.getImageUrl())
                .structuredContent(fromJson(entity.getStructuredContentJson(), LearningMemory.StructuredContent.class))
                .chatHistory(fromJson(entity.getChatHistoryJson(), new TypeReference<List<LearningMemory.ChatMessage>>() {}))
                .bookmarks(fromJson(entity.getBookmarksJson(), new TypeReference<List<String>>() {}))
                .summary(entity.getSummary())
                .relatedTopics(fromJson(entity.getRelatedTopicsJson(), new TypeReference<List<String>>() {}))
                .earnedBadges(fromJson(entity.getEarnedBadgesJson(), new TypeReference<List<String>>() {}))
                .build();
        return memory;
    }

    // ─── Update bookmarks ────────────────────────────────────────────────────
    public void updateBookmarks(Long sessionId, List<String> bookmarks) {
        sessionRepository.findById(sessionId).ifPresent(entity -> {
            entity.setBookmarksJson(toJson(bookmarks));
            entity.setUpdatedAt(LocalDateTime.now());
            sessionRepository.save(entity);
        });
    }

    // ─── User Progress / Gamification ────────────────────────────────────────
    public UserProgressEntity getUserProgress() {
        return userProgressRepository.findByUsername(DEFAULT_USER)
                .orElseGet(() -> {
                    UserProgressEntity progress = UserProgressEntity.builder()
                            .username(DEFAULT_USER)
                            .totalPoints(0)
                            .totalSessions(0)
                            .totalCorrectAnswers(0)
                            .totalQuestions(0)
                            .currentStreak(0)
                            .longestStreak(0)
                            .lastActiveDate(LocalDate.now())
                            .allBadgesJson("[]")
                            .createdAt(LocalDate.now())
                            .updatedAt(LocalDate.now())
                            .build();
                    return userProgressRepository.save(progress);
                });
    }

    private void updateUserProgress(int points, int correctAnswers, int totalQuestions) {
        UserProgressEntity progress = getUserProgress();
        progress.setTotalPoints(progress.getTotalPoints() + points);
        progress.setTotalSessions(progress.getTotalSessions() + 1);
        progress.setTotalCorrectAnswers(progress.getTotalCorrectAnswers() + correctAnswers);
        progress.setTotalQuestions(progress.getTotalQuestions() + totalQuestions);

        // Update streak
        LocalDate today = LocalDate.now();
        LocalDate lastActive = progress.getLastActiveDate();
        if (lastActive != null && lastActive.plusDays(1).equals(today)) {
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
        } else if (lastActive == null || !lastActive.equals(today)) {
            progress.setCurrentStreak(1);
        }
        if (progress.getCurrentStreak() > progress.getLongestStreak()) {
            progress.setLongestStreak(progress.getCurrentStreak());
        }
        progress.setLastActiveDate(today);

        // Check for new badges
        List<String> badges = fromJson(progress.getAllBadgesJson(), new TypeReference<List<String>>() {});
        if (badges == null) badges = new ArrayList<>();

        if (progress.getTotalSessions() >= 1 && !badges.contains("First Steps")) badges.add("First Steps");
        if (progress.getTotalSessions() >= 5 && !badges.contains("Curious Learner")) badges.add("Curious Learner");
        if (progress.getTotalSessions() >= 10 && !badges.contains("Knowledge Seeker")) badges.add("Knowledge Seeker");
        if (progress.getCurrentStreak() >= 3 && !badges.contains("3-Day Streak")) badges.add("3-Day Streak");
        if (progress.getCurrentStreak() >= 7 && !badges.contains("7-Day Streak")) badges.add("7-Day Streak");
        if (progress.getTotalPoints() >= 100 && !badges.contains("Century Club")) badges.add("Century Club");
        if (progress.getTotalPoints() >= 500 && !badges.contains("Knowledge Master")) badges.add("Knowledge Master");
        if (progress.getTotalCorrectAnswers() >= 20 && !badges.contains("Quiz Champion")) badges.add("Quiz Champion");

        progress.setAllBadgesJson(toJson(badges));
        progress.setUpdatedAt(LocalDate.now());
        userProgressRepository.save(progress);
    }

    // ─── Points calculation ──────────────────────────────────────────────────
    private int calculatePoints(int correct, int total) {
        if (total == 0) return 10; // points for completing a session
        int base = correct * 15;
        int bonus = (correct == total) ? 20 : 0; // perfect score bonus
        return base + bonus + 10; // +10 for completing
    }

    // ─── JSON helpers ────────────────────────────────────────────────────────
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON serialization failed: {}", e.getMessage());
            return null;
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("JSON deserialization failed: {}", e.getMessage());
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("JSON deserialization failed: {}", e.getMessage());
            return null;
        }
    }
}
