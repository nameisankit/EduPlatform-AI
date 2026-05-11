package com.eduplatform.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Retry utility with exponential backoff for handling API rate limits
 */
@Component
public class RetryUtils {

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    @Value("${eduplatform.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${eduplatform.retry.initial-delay-ms:1000}")
    private long initialDelayMs;

    @Value("${eduplatform.retry.max-delay-ms:30000}")
    private long maxDelayMs;

    /**
     * Execute operation with retry logic and exponential backoff
     *
     * @param operation The operation to execute
     * @param operationName Name for logging purposes
     * @param <T> Return type
     * @return Result of the operation
     * @throws Exception if all retry attempts fail
     */
    public <T> T executeWithRetry(RetryableOperation<T> operation, String operationName) throws Exception {
        Exception lastException = null;
        long currentDelay = initialDelayMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.debug("Attempt {}/{} for operation: {}", attempt, maxAttempts, operationName);
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                String errorMessage = e.getMessage();

                // Check if it's a rate limit error
                boolean isRateLimit = errorMessage != null && 
                    (errorMessage.contains("rate_limit_exceeded") || 
                     errorMessage.contains("Rate limit reached") ||
                     errorMessage.contains("429"));

                if (isRateLimit && attempt < maxAttempts) {
                    // Extract wait time from error message if available
                    long waitTime = extractWaitTime(errorMessage, currentDelay);
                    log.warn("Rate limit hit for operation '{}'. Waiting {}ms before retry (attempt {}/{})",
                            operationName, waitTime, attempt, maxAttempts);
                    
                    try {
                        TimeUnit.MILLISECONDS.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }

                    // Exponential backoff with cap
                    currentDelay = Math.min(currentDelay * 2, maxDelayMs);
                } else if (attempt < maxAttempts) {
                    log.warn("Operation '{}' failed on attempt {}/{}. Retrying in {}ms. Error: {}",
                            operationName, attempt, maxAttempts, currentDelay, errorMessage);
                    
                    try {
                        TimeUnit.MILLISECONDS.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry delay", ie);
                    }

                    currentDelay = Math.min(currentDelay * 2, maxDelayMs);
                } else {
                    log.error("Operation '{}' failed after {} attempts. Last error: {}", 
                            operationName, maxAttempts, errorMessage);
                }
            }
        }

        throw lastException;
    }

    /**
     * Extract wait time from Groq rate limit error message
     * Format: "Please try again in 18.279999999s"
     */
    private long extractWaitTime(String errorMessage, long defaultDelay) {
        try {
            // Look for pattern like "in 18.279999999s" or "in 18s"
            int inIndex = errorMessage.indexOf("in ");
            if (inIndex != -1) {
                int sIndex = errorMessage.indexOf("s", inIndex);
                if (sIndex != -1) {
                    String timeStr = errorMessage.substring(inIndex + 3, sIndex).trim();
                    double seconds = Double.parseDouble(timeStr);
                    return (long) (seconds * 1000) + 1000; // Add 1 second buffer
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract wait time from error message, using default");
        }
        return defaultDelay;
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
}
