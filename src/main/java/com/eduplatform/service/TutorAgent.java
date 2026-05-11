package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 📖 Tutor Agent
 * Uses Pollinations.ai text API — FREE, no API key needed
 */
@Service
public class TutorAgent {

    private static final Logger log = LoggerFactory.getLogger(TutorAgent.class);
    private final PollinationsClient pollinationsClient;

    public TutorAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public LearningMemory run(LearningMemory memory) {
        log.debug("📖 TutorAgent generating lesson for topic={}", memory.getTopic());

        String langInstruction = "";
        if (memory.getLanguage() != null && !memory.getLanguage().equalsIgnoreCase("English")) {
            langInstruction = " Write the entire lesson in " + memory.getLanguage() + " language.";
        }

        String prompt = "You are an expert tutor. Create a comprehensive engaging lesson for: " +
                "Topic: " + memory.getTopic() + ", Level: " + memory.getLevel() + ". " +
                "Structure your lesson with these markdown sections: " +
                "## Introduction (hook the student with an interesting fact or question), " +
                "## Core Concepts (explain main ideas clearly with examples), " +
                "## How It Works (step-by-step explanation with analogies), " +
                "## Real-World Applications (3-4 practical examples), " +
                "## Summary (key takeaways in bullet points). " +
                "Make it engaging, clear, and appropriate for " + memory.getLevel() + " level students." + langInstruction;

        try {
            String lesson = pollinationsClient.generate(prompt);
            memory.setLesson(lesson);

            // Generate image prompt while we're at it
            String imagePrompt = memory.getTopic() + " educational diagram colorful infographic";
            memory.setImagePrompt(imagePrompt);

        } catch (Exception e) {
            log.error("Failed to generate lesson: {}", e.getMessage());
            memory.setLesson("## Lesson: " + memory.getTopic() + "\n\n" +
                    "## Introduction\nWelcome to this lesson on " + memory.getTopic() + ".\n\n" +
                    "## Core Concepts\nThis topic covers fundamental principles that are widely used.\n\n" +
                    "## How It Works\nThe process involves several key steps and components.\n\n" +
                    "## Real-World Applications\n- Used in industry\n- Applied in research\n- Practical implementations\n\n" +
                    "## Summary\n- Key concept 1\n- Key concept 2\n- Key concept 3");
            memory.setImagePrompt(memory.getTopic() + " educational diagram");
        }

        log.debug("📖 TutorAgent complete.");
        return memory;
    }

    /**
     * Chat follow-up — answer student questions about the lesson
     */
    public String chat(LearningMemory memory, String question) {
        log.debug("💬 TutorAgent chat for topic={}", memory.getTopic());

        String context = memory.getLesson() != null
                ? memory.getLesson().substring(0, Math.min(800, memory.getLesson().length()))
                : "";

        String prompt = "You are a helpful tutor for the topic: " + memory.getTopic() + " at " + memory.getLevel() + " level. " +
                "Context from the lesson: " + context + ". " +
                "Student question: " + question + ". " +
                "Give a clear, helpful, concise answer (2-4 paragraphs max). Use markdown formatting.";

        try {
            // Add to chat history
            LearningMemory.ChatMessage userMsg = LearningMemory.ChatMessage.builder()
                    .role("user").content(question).build();
            memory.getChatHistory().add(userMsg);

            String answer = pollinationsClient.generate(prompt);

            LearningMemory.ChatMessage assistantMsg = LearningMemory.ChatMessage.builder()
                    .role("assistant").content(answer).build();
            memory.getChatHistory().add(assistantMsg);

            return answer;
        } catch (Exception e) {
            log.error("Chat failed: {}", e.getMessage());
            return "I apologize, I'm having trouble connecting right now. Please try again in a moment.";
        }
    }
}
