package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 📋 Curriculum Agent
 * Uses Pollinations.ai text API — FREE, no API key needed
 */
@Service
public class CurriculumAgent {

    private static final Logger log = LoggerFactory.getLogger(CurriculumAgent.class);
    private final PollinationsClient pollinationsClient;

    public CurriculumAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public LearningMemory run(LearningMemory memory) {
        log.debug("📋 CurriculumAgent running for topic={}, level={}", memory.getTopic(), memory.getLevel());

        String prompt = buildPrompt(memory.getTopic(), memory.getLevel(), memory.getLanguage());

        try {
            String curriculum = pollinationsClient.generate(prompt);
            memory.setCurriculum(curriculum);
        } catch (Exception e) {
            log.error("Failed to generate curriculum: {}", e.getMessage());
            memory.setCurriculum("## Learning Roadmap: " + memory.getTopic() + "\n\n" +
                    "### Learning Objectives\n- Understand core concepts\n- Apply knowledge practically\n\n" +
                    "### Module 1: Introduction\n- Basic concepts and overview\n\n" +
                    "### Module 2: Core Concepts\n- Deep dive into fundamentals\n\n" +
                    "### Module 3: Advanced Topics\n- Real-world applications");
        }

        // Extract structured content (key points, prerequisites)
        LearningMemory.StructuredContent structured = extractStructured(memory.getTopic(), memory.getLevel());
        memory.setStructuredContent(structured);

        log.debug("📋 CurriculumAgent complete.");
        return memory;
    }

    private String buildPrompt(String topic, String level, String language) {
        String langInstruction = "";
        if (language != null && !language.equalsIgnoreCase("English")) {
            langInstruction = " Write the entire curriculum in " + language + " language.";
        }
        return "You are an expert curriculum designer. Create a structured learning roadmap for: " +
                "Topic: " + topic + ", Level: " + level + ". " +
                "Format your response as a clear roadmap with markdown headings: " +
                "## Learning Objectives (3-4 bullet points), " +
                "## Prerequisites (what student should know first), " +
                "## Module Breakdown with Module 1 Introduction, Module 2 Core Concepts, Module 3 Advanced Topics, " +
                "## Estimated Time. Be concise and educational." + langInstruction;
    }

    private LearningMemory.StructuredContent extractStructured(String topic, String level) {
        try {
            String prompt = "List exactly 4 key points and 3 prerequisites for learning " + topic +
                    " at " + level + " level. Also give a 2-sentence explanation. " +
                    "Format: KEY_POINTS: point1|point2|point3|point4 PREREQUISITES: pre1|pre2|pre3 EXPLANATION: your explanation here";

            String response = pollinationsClient.generate(prompt);

            List<String> keyPoints = List.of(
                    "Core concepts of " + topic,
                    "Practical applications",
                    "Best practices and patterns",
                    "Real-world examples"
            );
            List<String> prerequisites = List.of(
                    "Basic programming knowledge",
                    "Analytical thinking",
                    "Willingness to learn"
            );
            String explanation = topic + " is an important subject with many real-world applications. " +
                    "This " + level + " course will help you understand the fundamentals and apply them practically.";

            // Try to parse structured response
            if (response.contains("KEY_POINTS:")) {
                try {
                    String kpSection = response.substring(response.indexOf("KEY_POINTS:") + 11);
                    if (kpSection.contains("PREREQUISITES:")) {
                        String kpStr = kpSection.substring(0, kpSection.indexOf("PREREQUISITES:")).trim();
                        keyPoints = Arrays.asList(kpStr.split("\\|"));
                    }
                } catch (Exception ignored) {}
            }
            if (response.contains("PREREQUISITES:")) {
                try {
                    String preSection = response.substring(response.indexOf("PREREQUISITES:") + 14);
                    if (preSection.contains("EXPLANATION:")) {
                        String preStr = preSection.substring(0, preSection.indexOf("EXPLANATION:")).trim();
                        prerequisites = Arrays.asList(preStr.split("\\|"));
                    }
                } catch (Exception ignored) {}
            }
            if (response.contains("EXPLANATION:")) {
                try {
                    explanation = response.substring(response.indexOf("EXPLANATION:") + 12).trim();
                } catch (Exception ignored) {}
            }

            return LearningMemory.StructuredContent.builder()
                    .keyPoints(keyPoints)
                    .prerequisites(prerequisites)
                    .explanation(explanation)
                    .build();

        } catch (Exception e) {
            log.warn("Could not extract structured content: {}", e.getMessage());
            return LearningMemory.StructuredContent.builder()
                    .keyPoints(List.of("Core concepts", "Practical applications", "Best practices", "Real-world examples"))
                    .prerequisites(List.of("Basic knowledge", "Analytical thinking"))
                    .explanation("An introduction to " + topic + " for " + level + " learners.")
                    .build();
        }
    }
}
