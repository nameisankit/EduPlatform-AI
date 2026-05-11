package com.eduplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * LearningMemory — equivalent to Python's memory/state object
 * Stored in HTTP session, passed between agents in the pipeline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningMemory implements Serializable {

    // Core inputs
    private String topic;
    private String level;
    private String language;        // Multi-language support

    // Agent outputs
    private String curriculum;      // 📋 Curriculum Agent output
    private String lesson;          // 📖 Tutor Agent output
    private QuizData quiz;          // ❓ Quiz Agent output
    private String feedback;        // ✅ Feedback Agent output

    // GenAI extras
    private String imagePrompt;     // prompt used for image generation
    private String imageUrl;        // Pollinations image URL

    // Structured content from LLM (key_points, explanation etc.)
    private StructuredContent structuredContent;

    // Chat history for Ask Tutor feature
    private List<ChatMessage> chatHistory;

    // Summary (TL;DR)
    private String summary;

    // Related topics suggestions
    private List<String> relatedTopics;

    // Bookmarks
    private List<String> bookmarks;

    // Gamification
    private List<String> earnedBadges;
    private int points;
    private int correctAnswers;
    private int totalQuestions;

    // Difficulty adaptation
    private String suggestedLevel;  // Auto-suggested level for next session

    // Session ID for persistence
    private Long sessionId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StructuredContent implements Serializable {
        private String explanation;
        private List<String> keyPoints;
        private List<String> prerequisites;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuizData implements Serializable {
        private List<Question> questions;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Question implements Serializable {
            private int id;
            private String type;       // "mcq" or "short_answer"
            private String question;
            private List<String> options; // only for MCQ
            private String correctAnswer; // A, B, C, D
            private String explanation;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChatMessage implements Serializable {
        private String role;    // "user" or "assistant"
        private String content;
    }
}
