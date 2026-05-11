package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ✅ Feedback Agent
 * Uses Pollinations.ai text API — FREE, no API key needed
 */
@Service
public class FeedbackAgent {

    private static final Logger log = LoggerFactory.getLogger(FeedbackAgent.class);
    private final PollinationsClient pollinationsClient;

    public FeedbackAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public LearningMemory run(LearningMemory memory, Map<Integer, String> answers) {
        log.debug("✅ FeedbackAgent evaluating answers for topic={}", memory.getTopic());

        String answersText = answers.entrySet().stream()
                .map(e -> "Q" + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));

        String questionsContext = "";
        if (memory.getQuiz() != null && memory.getQuiz().getQuestions() != null) {
            questionsContext = memory.getQuiz().getQuestions().stream()
                    .map(q -> "Q" + q.getId() + ": " + q.getQuestion() + " (Correct: " + q.getCorrectAnswer() + ")")
                    .collect(Collectors.joining("; "));
        }

        String prompt = "You are an encouraging learning coach evaluating quiz answers about " + memory.getTopic() + " at " + memory.getLevel() + " level. " +
                "Questions and correct answers: " + questionsContext + ". " +
                "Student answers: " + answersText + ". " +
                "Provide detailed feedback in markdown with these sections: " +
                "## Overall Assessment (encouraging summary), " +
                "## What You Got Right (acknowledge correct answers), " +
                "## Areas to Review (explain correct concepts for wrong answers), " +
                "## Key Takeaways (3-4 important concepts to remember), " +
                "## Next Steps (suggested next topics). " +
                "Be encouraging and constructive. Focus on learning, not just correctness.";

        try {
            String feedback = pollinationsClient.generate(prompt);
            memory.setFeedback(feedback);
        } catch (Exception e) {
            log.error("Failed to generate feedback: {}", e.getMessage());
            memory.setFeedback("## 📊 Overall Assessment\n\nThank you for completing the quiz on **" + memory.getTopic() + "**!\n\n" +
                    "## 🎯 Key Takeaways\n- Review the core concepts from the lesson\n- Practice with real-world examples\n- Keep exploring this fascinating topic!\n\n" +
                    "## 🚀 Next Steps\n- Dive deeper into advanced topics\n- Try hands-on projects\n- Explore related subjects");
        }

        log.debug("✅ FeedbackAgent complete.");

        // Difficulty adaptation
        int correct = 0;
        int total = memory.getQuiz() != null && memory.getQuiz().getQuestions() != null
                ? memory.getQuiz().getQuestions().size() : 0;
        if (total > 0) {
            for (LearningMemory.QuizData.Question q : memory.getQuiz().getQuestions()) {
                String answer = answers.get(q.getId());
                if (answer != null && answer.trim().equalsIgnoreCase(q.getCorrectAnswer().trim())) {
                    correct++;
                }
            }
            double ratio = (double) correct / total;
            String currentLevel = memory.getLevel();
            if (ratio >= 0.8 && !"advanced".equals(currentLevel)) {
                memory.setSuggestedLevel(switch (currentLevel) {
                    case "beginner" -> "intermediate";
                    case "intermediate" -> "advanced";
                    default -> "advanced";
                });
            } else if (ratio < 0.4 && !"beginner".equals(currentLevel)) {
                memory.setSuggestedLevel(switch (currentLevel) {
                    case "advanced" -> "intermediate";
                    case "intermediate" -> "beginner";
                    default -> "beginner";
                });
            }
            memory.setCorrectAnswers(correct);
            memory.setTotalQuestions(total);
        }

        return memory;
    }
}
