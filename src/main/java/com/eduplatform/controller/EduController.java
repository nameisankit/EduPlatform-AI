package com.eduplatform.controller;

import com.eduplatform.model.LearningMemory;
import com.eduplatform.model.SessionEntity;
import com.eduplatform.model.UserProgressEntity;
import com.eduplatform.service.EducationPipeline;
import com.eduplatform.service.PdfExportService;
import com.eduplatform.service.SessionPersistenceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main controller — handles all page routes
 * Replaces Streamlit's app.py routing
 */
@Controller
public class EduController {

    private static final Logger log = LoggerFactory.getLogger(EduController.class);
    private static final String SESSION_MEMORY_KEY = "learning_memory";

    private final EducationPipeline pipeline;
    private final PdfExportService pdfExportService;
    private final ObjectMapper objectMapper;

    public EduController(EducationPipeline pipeline, PdfExportService pdfExportService, ObjectMapper objectMapper) {
        this.pipeline = pipeline;
        this.pdfExportService = pdfExportService;
        this.objectMapper = objectMapper;
    }

    private void addProgressToModel(Model model) {
        SessionPersistenceService ps = pipeline.getPersistenceService();
        UserProgressEntity progress = ps.getUserProgress();
        model.addAttribute("userProgress", progress);
        model.addAttribute("recentSessions", ps.getAllSessions().stream().limit(10).toList());

        // Parse badges JSON to List for Thymeleaf
        List<String> badges = new ArrayList<>();
        if (progress.getAllBadgesJson() != null && !progress.getAllBadgesJson().isBlank()) {
            try {
                badges = objectMapper.readValue(progress.getAllBadgesJson(), new TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        model.addAttribute("earnedBadges", badges);
    }

    // ─── Landing page ──────────────────────────────────────────────────────
    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        LearningMemory memory = (LearningMemory) session.getAttribute(SESSION_MEMORY_KEY);
        model.addAttribute("memory", memory);
        model.addAttribute("activeTab", "curriculum");
        model.addAttribute("quizSubmitted", false);

        addProgressToModel(model);

        return "index";
    }

    // ─── Start Learning — runs the full pipeline ───────────────────────────
    @PostMapping("/learn")
    public String startLearning(
            @RequestParam String topic,
            @RequestParam String level,
            @RequestParam(defaultValue = "true") boolean generateImage,
            @RequestParam(required = false, defaultValue = "English") String language,
            HttpSession session,
            Model model
    ) {
        log.info("Starting learning for topic='{}', level='{}', language='{}'", topic, level, language);

        try {
            // Clear previous session
            session.removeAttribute(SESSION_MEMORY_KEY);

            // Run pipeline
            LearningMemory memory = pipeline.runFullPipeline(topic, level, language);
            memory.setBookmarks(new java.util.ArrayList<>());

            // Generate image if requested
            if (generateImage) {
                pipeline.generateImage(memory);
            }

            // Save to persistent storage
            SessionPersistenceService ps = pipeline.getPersistenceService();
            SessionEntity entity = ps.saveSession(memory, 0, 0);
            if (entity != null) {
                memory.setSessionId(entity.getId());
            }

            session.setAttribute(SESSION_MEMORY_KEY, memory);
            model.addAttribute("memory", memory);
            model.addAttribute("activeTab", "curriculum");
            model.addAttribute("quizSubmitted", false);
            model.addAttribute("success", "Pipeline complete!");

            addProgressToModel(model);

        } catch (Exception e) {
            log.error("Pipeline error: {}", e.getMessage(), e);
            model.addAttribute("error", "Error running pipeline: " + e.getMessage());
        }

        return "index";
    }

    // ─── Submit Quiz ───────────────────────────────────────────────────────
    @PostMapping("/quiz/submit")
    public String submitQuiz(
            @RequestParam Map<String, String> allParams,
            HttpSession session,
            Model model
    ) {
        LearningMemory memory = (LearningMemory) session.getAttribute(SESSION_MEMORY_KEY);
        if (memory == null) {
            return "redirect:/";
        }

        // Parse answers from form params (keys like "q1", "q2", etc.)
        Map<Integer, String> answers = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("q")) {
                try {
                    int qId = Integer.parseInt(key.substring(1));
                    answers.put(qId, value);
                } catch (NumberFormatException ignored) {}
            }
        });

        // Get feedback (also calculates correctAnswers, totalQuestions, suggestedLevel)
        memory = pipeline.getFeedback(memory, answers);

        // Save updated session with quiz results
        SessionPersistenceService ps = pipeline.getPersistenceService();
        if (memory.getSessionId() != null) {
            ps.saveSession(memory, memory.getCorrectAnswers(), memory.getTotalQuestions());
        }

        session.setAttribute(SESSION_MEMORY_KEY, memory);

        model.addAttribute("memory", memory);
        model.addAttribute("activeTab", "quiz");
        model.addAttribute("quizSubmitted", true);
        model.addAttribute("submittedAnswers", answers);

        addProgressToModel(model);

        return "index";
    }

    // ─── Chat API (AJAX) ───────────────────────────────────────────────────
    @PostMapping("/chat")
    @ResponseBody
    public Map<String, String> chat(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        LearningMemory memory = (LearningMemory) session.getAttribute(SESSION_MEMORY_KEY);
        if (memory == null) {
            return Map.of("error", "No active learning session");
        }

        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return Map.of("error", "Empty question");
        }

        String answer = pipeline.tutorChat(memory, question);
        session.setAttribute(SESSION_MEMORY_KEY, memory); // save updated chat history

        return Map.of("answer", answer);
    }

    // ─── Reset session ─────────────────────────────────────────────────────
    @PostMapping("/reset")
    public String reset(HttpSession session) {
        session.removeAttribute(SESSION_MEMORY_KEY);
        return "redirect:/";
    }

    // ─── PDF Export ─────────────────────────────────────────────────────────
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(HttpSession session) {
        LearningMemory memory = (LearningMemory) session.getAttribute(SESSION_MEMORY_KEY);
        if (memory == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = pdfExportService.generatePdf(memory);
        if (pdf == null) {
            return ResponseEntity.internalServerError().build();
        }

        String filename = memory.getTopic().replaceAll("[^a-zA-Z0-9]", "_") + "_lesson.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ─── Bookmark API (AJAX) ───────────────────────────────────────────────
    @PostMapping("/bookmark")
    @ResponseBody
    public Map<String, Object> toggleBookmark(
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        LearningMemory memory = (LearningMemory) session.getAttribute(SESSION_MEMORY_KEY);
        if (memory == null) {
            return Map.of("error", "No active session");
        }

        String text = body.get("text");
        if (text == null || text.isBlank()) {
            return Map.of("error", "Empty bookmark text");
        }

        if (memory.getBookmarks() == null) {
            memory.setBookmarks(new java.util.ArrayList<>());
        }

        if (memory.getBookmarks().contains(text)) {
            memory.getBookmarks().remove(text);
        } else {
            memory.getBookmarks().add(text);
        }

        session.setAttribute(SESSION_MEMORY_KEY, memory);

        // Update DB
        if (memory.getSessionId() != null) {
            pipeline.getPersistenceService().updateBookmarks(memory.getSessionId(), memory.getBookmarks());
        }

        return Map.of("bookmarks", memory.getBookmarks());
    }

    // ─── Load saved session ────────────────────────────────────────────────
    @PostMapping("/session/load")
    public String loadSession(@RequestParam Long sessionId, HttpSession session, Model model) {
        SessionPersistenceService ps = pipeline.getPersistenceService();
        var entity = ps.getSession(sessionId);
        if (entity.isPresent()) {
            LearningMemory memory = ps.toMemory(entity.get());
            memory.setSessionId(sessionId);
            session.setAttribute(SESSION_MEMORY_KEY, memory);
            model.addAttribute("memory", memory);
            model.addAttribute("quizSubmitted", false);
            model.addAttribute("success", "Session loaded!");
        }
        return "redirect:/";
    }

    // ─── Dashboard data API (AJAX) ─────────────────────────────────────────
    @GetMapping("/api/progress")
    @ResponseBody
    public Map<String, Object> getProgress() {
        SessionPersistenceService ps = pipeline.getPersistenceService();
        UserProgressEntity progress = ps.getUserProgress();
        return Map.of(
                "totalPoints", progress.getTotalPoints(),
                "totalSessions", progress.getTotalSessions(),
                "currentStreak", progress.getCurrentStreak(),
                "longestStreak", progress.getLongestStreak(),
                "totalCorrectAnswers", progress.getTotalCorrectAnswers(),
                "totalQuestions", progress.getTotalQuestions()
        );
    }
}
