package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Summary Agent - generates TL;DR for lessons
 */
@Service
public class SummaryAgent {

    private static final Logger log = LoggerFactory.getLogger(SummaryAgent.class);
    private final PollinationsClient pollinationsClient;

    public SummaryAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public String generateSummary(LearningMemory memory) {
        log.debug("SummaryAgent generating TL;DR for topic={}", memory.getTopic());

        String lessonText = memory.getLesson() != null
                ? memory.getLesson().substring(0, Math.min(2000, memory.getLesson().length()))
                : "No lesson content available.";

        String prompt = "Create a concise TL;DR summary (3-5 bullet points) for this lesson about " +
                memory.getTopic() + " at " + memory.getLevel() + " level. Lesson content: " + lessonText +
                ". Format as bullet points starting with -. Keep each point to one sentence.";

        try {
            String summary = pollinationsClient.generate(prompt);
            memory.setSummary(summary);
            return summary;
        } catch (Exception e) {
            log.error("Failed to generate summary: {}", e.getMessage());
            String fallback = "- Core concepts of " + memory.getTopic() + "\n- Practical applications and examples\n- Key principles to remember";
            memory.setSummary(fallback);
            return fallback;
        }
    }
}
