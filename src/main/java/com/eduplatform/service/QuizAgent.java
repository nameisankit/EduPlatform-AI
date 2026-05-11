package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ❓ Quiz Agent
 * Uses Pollinations.ai text API — FREE, no API key needed
 */
@Service
public class QuizAgent {

    private static final Logger log = LoggerFactory.getLogger(QuizAgent.class);
    private final PollinationsClient pollinationsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public LearningMemory run(LearningMemory memory) {
        log.debug("❓ QuizAgent generating quiz for topic={}", memory.getTopic());

        String langInstruction = "";
        if (memory.getLanguage() != null && !memory.getLanguage().equalsIgnoreCase("English")) {
            langInstruction = " Write all questions and explanations in " + memory.getLanguage() + " language.";
        }

        String prompt = "Create exactly 3 quiz questions about " + memory.getTopic() + " for " + memory.getLevel() + " level students. " +
                "Return ONLY valid JSON, no extra text, no markdown code blocks. " +
                "Format: {\"questions\":[" +
                "{\"id\":1,\"type\":\"mcq\",\"question\":\"Question here?\",\"options\":[\"A. option\",\"B. option\",\"C. option\",\"D. option\"],\"correct_answer\":\"A\",\"explanation\":\"Why A is correct\"}," +
                "{\"id\":2,\"type\":\"mcq\",\"question\":\"Question here?\",\"options\":[\"A. option\",\"B. option\",\"C. option\",\"D. option\"],\"correct_answer\":\"B\",\"explanation\":\"Why B is correct\"}," +
                "{\"id\":3,\"type\":\"short_answer\",\"question\":\"Open-ended question here?\",\"options\":[],\"correct_answer\":\"Expected answer\",\"explanation\":\"Explanation\"}" +
                "]}" + langInstruction;

        try {
            String json = pollinationsClient.generate(prompt);
            LearningMemory.QuizData quizData = parseQuiz(json, memory.getTopic());
            memory.setQuiz(quizData);
            log.debug("❓ QuizAgent complete. {} questions generated.", quizData.getQuestions().size());
        } catch (Exception e) {
            log.error("Failed to generate quiz: {}", e.getMessage());
            memory.setQuiz(buildFallbackQuiz(memory.getTopic()));
        }

        return memory;
    }

    private LearningMemory.QuizData parseQuiz(String json, String topic) {
        try {
            // Clean JSON — remove markdown code fences if present
            String cleaned = json
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "")
                    .trim();

            // Find JSON object start
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start != -1 && end != -1 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode questionsNode = root.get("questions");

            List<LearningMemory.QuizData.Question> questions = new ArrayList<>();
            if (questionsNode != null && questionsNode.isArray()) {
                for (JsonNode q : questionsNode) {
                    List<String> options = new ArrayList<>();
                    if (q.has("options") && q.get("options").isArray()) {
                        q.get("options").forEach(o -> options.add(o.asText()));
                    }
                    questions.add(LearningMemory.QuizData.Question.builder()
                            .id(q.has("id") ? q.get("id").asInt() : questions.size() + 1)
                            .type(q.has("type") ? q.get("type").asText() : "mcq")
                            .question(q.has("question") ? q.get("question").asText() : "")
                            .options(options)
                            .correctAnswer(q.has("correct_answer") ? q.get("correct_answer").asText() : "A")
                            .explanation(q.has("explanation") ? q.get("explanation").asText() : "")
                            .build());
                }
            }

            if (!questions.isEmpty()) {
                return LearningMemory.QuizData.builder().questions(questions).build();
            }
        } catch (Exception e) {
            log.warn("Could not parse quiz JSON: {}", e.getMessage());
        }
        return buildFallbackQuiz(topic);
    }

    private LearningMemory.QuizData buildFallbackQuiz(String topic) {
        return LearningMemory.QuizData.builder()
                .questions(Arrays.asList(
                        LearningMemory.QuizData.Question.builder()
                                .id(1).type("mcq")
                                .question("What is the primary purpose of " + topic + "?")
                                .options(Arrays.asList(
                                        "A. To solve complex problems efficiently",
                                        "B. To create visual designs",
                                        "C. To manage databases",
                                        "D. To write documentation"))
                                .correctAnswer("A")
                                .explanation("The primary purpose focuses on efficient problem solving.")
                                .build(),
                        LearningMemory.QuizData.Question.builder()
                                .id(2).type("mcq")
                                .question("Which of the following best describes " + topic + "?")
                                .options(Arrays.asList(
                                        "A. A hardware component",
                                        "B. A systematic approach or methodology",
                                        "C. A programming language",
                                        "D. A type of database"))
                                .correctAnswer("B")
                                .explanation(topic + " is best described as a systematic approach.")
                                .build(),
                        LearningMemory.QuizData.Question.builder()
                                .id(3).type("short_answer")
                                .question("Describe one real-world application of " + topic + " and explain its impact.")
                                .options(List.of())
                                .correctAnswer("Should demonstrate understanding of practical applications")
                                .explanation("Look for specific examples with clear impact explanation")
                                .build()
                ))
                .build();
    }

    public boolean evaluateMcq(LearningMemory.QuizData.Question question, String selectedOption) {
        if (selectedOption == null || selectedOption.isBlank()) return false;
        String selected = selectedOption.trim().substring(0, 1).toUpperCase();
        return selected.equals(question.getCorrectAnswer().trim().toUpperCase());
    }
}
