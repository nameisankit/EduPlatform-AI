package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * PDF Export Service - generates PDF from lesson/curriculum content
 */
@Service
public class PdfExportService {

    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);

    public byte[] generatePdf(LearningMemory memory) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.DARK_GRAY);
            Font headingFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(45, 106, 159));
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
            Font badgeFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

            // Header
            Paragraph title = new Paragraph("EduPlatform AI - " + memory.getTopic(), titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            Paragraph subtitle = new Paragraph("Level: " + memory.getLevel() +
                    (memory.getLanguage() != null ? " | Language: " + memory.getLanguage() : ""), bodyFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(20);
            document.add(subtitle);

            // Summary section
            if (memory.getSummary() != null) {
                document.add(new Paragraph("TL;DR Summary", headingFont));
                document.add(Chunk.NEWLINE);
                addMarkdownText(document, memory.getSummary(), bodyFont);
                document.add(Chunk.NEWLINE);
            }

            // Curriculum section
            if (memory.getCurriculum() != null) {
                document.add(new Paragraph("Learning Roadmap", headingFont));
                document.add(Chunk.NEWLINE);
                addMarkdownText(document, memory.getCurriculum(), bodyFont);
                document.add(Chunk.NEWLINE);
            }

            // Lesson section
            if (memory.getLesson() != null) {
                document.add(new Paragraph("Lesson", headingFont));
                document.add(Chunk.NEWLINE);
                addMarkdownText(document, memory.getLesson(), bodyFont);
                document.add(Chunk.NEWLINE);
            }

            // Key Points table
            if (memory.getStructuredContent() != null && memory.getStructuredContent().getKeyPoints() != null) {
                document.add(new Paragraph("Key Points", headingFont));
                document.add(Chunk.NEWLINE);

                PdfPTable table = new PdfPTable(1);
                table.setWidthPercentage(100);
                for (String point : memory.getStructuredContent().getKeyPoints()) {
                    PdfPCell cell = new PdfPCell(new Phrase(point, bodyFont));
                    cell.setBackgroundColor(new BaseColor(227, 242, 253));
                    cell.setPadding(8);
                    cell.setBorderWidth(0.5f);
                    table.addCell(cell);
                }
                document.add(table);
                document.add(Chunk.NEWLINE);
            }

            // Quiz section
            if (memory.getQuiz() != null && memory.getQuiz().getQuestions() != null) {
                document.add(new Paragraph("Quiz Questions", headingFont));
                document.add(Chunk.NEWLINE);
                for (LearningMemory.QuizData.Question q : memory.getQuiz().getQuestions()) {
                    document.add(new Paragraph("Q" + q.getId() + ": " + q.getQuestion(), bodyFont));
                    if (q.getOptions() != null) {
                        for (String opt : q.getOptions()) {
                            document.add(new Paragraph("  - " + opt, bodyFont));
                        }
                    }
                    document.add(new Paragraph("Answer: " + q.getCorrectAnswer(), bodyFont));
                    document.add(Chunk.NEWLINE);
                }
            }

            // Feedback section
            if (memory.getFeedback() != null) {
                document.add(new Paragraph("Feedback", headingFont));
                document.add(Chunk.NEWLINE);
                addMarkdownText(document, memory.getFeedback(), bodyFont);
            }

            // Related topics
            if (memory.getRelatedTopics() != null && !memory.getRelatedTopics().isEmpty()) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("Suggested Next Topics", headingFont));
                document.add(Chunk.NEWLINE);
                for (String topic : memory.getRelatedTopics()) {
                    document.add(new Paragraph("  -> " + topic, bodyFont));
                }
            }

            // Footer
            document.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph("Generated by EduPlatform AI", new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage(), e);
            return null;
        }
    }

    private void addMarkdownText(Document document, String text, Font font) throws Exception {
        String[] lines = text.split("\n");
        for (String line : lines) {
            String cleaned = line
                    .replaceAll("^#{1,6}\\s+", "")
                    .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                    .replaceAll("\\*(.*?)\\*", "$1")
                    .replaceAll("`(.*?)`", "$1")
                    .replaceAll("^[-*]\\s+", "  • ")
                    .trim();

            if (cleaned.isBlank()) {
                document.add(Chunk.NEWLINE);
            } else {
                Paragraph p = new Paragraph(cleaned, font);
                p.setSpacingAfter(3);
                document.add(p);
            }
        }
    }
}
