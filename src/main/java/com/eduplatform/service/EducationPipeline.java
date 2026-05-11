package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

/**
 * 🧠 Education Pipeline
 * Orchestrates all agents: Curriculum → Tutor → Quiz → Feedback
 * Uses Pollinations.ai — FREE, no API key needed
 */
@Service
public class EducationPipeline {

    private static final Logger log = LoggerFactory.getLogger(EducationPipeline.class);

    private final CurriculumAgent curriculumAgent;
    private final TutorAgent tutorAgent;
    private final QuizAgent quizAgent;
    private final FeedbackAgent feedbackAgent;
    private final SummaryAgent summaryAgent;
    private final RelatedTopicsAgent relatedTopicsAgent;
    private final SessionPersistenceService persistenceService;

    @Value("${eduplatform.image.base-url}")
    private String imageBaseUrl;

    @Value("${eduplatform.image.width:800}")
    private int imageWidth;

    @Value("${eduplatform.image.height:450}")
    private int imageHeight;

    public EducationPipeline(
            CurriculumAgent curriculumAgent,
            TutorAgent tutorAgent,
            QuizAgent quizAgent,
            FeedbackAgent feedbackAgent,
            SummaryAgent summaryAgent,
            RelatedTopicsAgent relatedTopicsAgent,
            SessionPersistenceService persistenceService
    ) {
        this.curriculumAgent = curriculumAgent;
        this.tutorAgent = tutorAgent;
        this.quizAgent = quizAgent;
        this.feedbackAgent = feedbackAgent;
        this.summaryAgent = summaryAgent;
        this.relatedTopicsAgent = relatedTopicsAgent;
        this.persistenceService = persistenceService;
    }

    public LearningMemory runFullPipeline(String topic, String level, String language) {
        log.info("🚀 Starting pipeline for topic='{}', level='{}', language='{}'", topic, level, language);

        LearningMemory memory = LearningMemory.builder()
                .topic(topic)
                .level(level)
                .language(language != null ? language : "English")
                .chatHistory(new ArrayList<>())
                .bookmarks(new ArrayList<>())
                .build();

        log.info("  Step 1/5: Curriculum Agent...");
        memory = curriculumAgent.run(memory);

        log.info("  Step 2/5: Tutor Agent...");
        memory = tutorAgent.run(memory);

        log.info("  Step 3/5: Quiz Agent...");
        memory = quizAgent.run(memory);

        log.info("  Step 4/5: Summary Agent...");
        summaryAgent.generateSummary(memory);

        log.info("  Step 5/5: Related Topics Agent...");
        relatedTopicsAgent.suggestRelatedTopics(memory);

        log.info("✅ Pipeline complete for topic='{}'", topic);
        return memory;
    }

    public String generateImage(LearningMemory memory) {
        try {
            String prompt = memory.getImagePrompt() != null
                    ? memory.getImagePrompt()
                    : memory.getTopic() + " educational diagram colorful infographic";

            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            String imageUrl = imageBaseUrl + encodedPrompt
                    + "?width=" + imageWidth
                    + "&height=" + imageHeight
                    + "&nologo=true";

            log.info("Downloading image for topic='{}' from: {}", memory.getTopic(), imageUrl);

            // Download image server-side and convert to base64
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(30))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(imageUrl))
                    .timeout(java.time.Duration.ofSeconds(120))
                    .header("User-Agent", "EduPlatform-AI/1.0")
                    .GET()
                    .build();

            java.net.http.HttpResponse<byte[]> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200 && response.body() != null && response.body().length > 0) {
                String contentType = response.headers().firstValue("Content-Type").orElse("image/png");
                String base64 = java.util.Base64.getEncoder().encodeToString(response.body());
                String dataUri = "data:" + contentType + ";base64," + base64;

                log.info("Image downloaded successfully, size={} bytes, embedding as base64", response.body().length);
                memory.setImageUrl(dataUri);
                return dataUri;
            } else {
                log.warn("Image download returned status={}, falling back to URL", response.statusCode());
                memory.setImageUrl(imageUrl);
                return imageUrl;
            }
        } catch (Exception e) {
            log.error("Image generation failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public LearningMemory getFeedback(LearningMemory memory, Map<Integer, String> answers) {
        log.info("✅ Getting feedback for topic='{}'", memory.getTopic());
        return feedbackAgent.run(memory, answers);
    }

    public String tutorChat(LearningMemory memory, String question) {
        return tutorAgent.chat(memory, question);
    }

    public SessionPersistenceService getPersistenceService() {
        return persistenceService;
    }
}
