package com.eduplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 🤖 Pollinations.ai Text Client
 * FREE — no API key needed
 * API: https://text.pollinations.ai/{prompt}
 */
@Component
public class PollinationsClient {

    private static final Logger log = LoggerFactory.getLogger(PollinationsClient.class);
    private static final String TEXT_API_BASE = "https://text.pollinations.ai/";
    private static final int MAX_RETRIES = 3;

    private final HttpClient httpClient;

    public PollinationsClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * Call Pollinations.ai text API with a prompt.
     * Uses model=openai for best quality responses.
     */
    public String generate(String prompt) {
        String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
        // Add system instruction via seed for consistent educational content
        String url = TEXT_API_BASE + encodedPrompt + "?model=openai&seed=42";

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                log.debug("Pollinations text API call, attempt {}/{}", attempt, MAX_RETRIES);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(120))
                        .header("User-Agent", "EduPlatform-AI/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();
                    if (body != null && !body.isBlank()) {
                        log.debug("Pollinations response received, length={}", body.length());
                        return body.trim();
                    }
                }

                log.warn("Pollinations returned status={}, retrying...", response.statusCode());
                Thread.sleep(2000L * attempt);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", ie);
            } catch (IOException e) {
                log.warn("Pollinations API error on attempt {}: {}", attempt, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try { Thread.sleep(1500L * attempt); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        throw new RuntimeException("Pollinations.ai text API failed after " + MAX_RETRIES + " attempts");
    }
}
