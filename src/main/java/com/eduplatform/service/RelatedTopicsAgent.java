package com.eduplatform.service;

import com.eduplatform.model.LearningMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Related Topics Agent - suggests what to learn next
 */
@Service
public class RelatedTopicsAgent {

    private static final Logger log = LoggerFactory.getLogger(RelatedTopicsAgent.class);
    private final PollinationsClient pollinationsClient;

    public RelatedTopicsAgent(PollinationsClient pollinationsClient) {
        this.pollinationsClient = pollinationsClient;
    }

    public List<String> suggestRelatedTopics(LearningMemory memory) {
        log.debug("RelatedTopicsAgent suggesting topics related to={}", memory.getTopic());

        String prompt = "Suggest exactly 5 related topics that someone should study after learning about " +
                memory.getTopic() + " at " + memory.getLevel() + " level. " +
                "Format your response as a simple list with each topic on a new line starting with a number. " +
                "Example: 1. Topic Name. No extra explanation needed.";

        try {
            String response = pollinationsClient.generate(prompt);
            List<String> topics = Arrays.stream(response.split("\n"))
                    .map(line -> line.replaceAll("^\\d+\\.\\s*", "").trim())
                    .filter(line -> !line.isBlank() && line.length() < 100)
                    .limit(5)
                    .toList();

            if (topics.isEmpty()) {
                topics = getDefaultRelatedTopics(memory.getTopic());
            }

            memory.setRelatedTopics(topics);
            return topics;
        } catch (Exception e) {
            log.error("Failed to suggest related topics: {}", e.getMessage());
            List<String> defaults = getDefaultRelatedTopics(memory.getTopic());
            memory.setRelatedTopics(defaults);
            return defaults;
        }
    }

    private List<String> getDefaultRelatedTopics(String topic) {
        return List.of(
                "Advanced " + topic,
                "Practical applications of " + topic,
                "History of " + topic,
                topic + " in modern industry",
                "Interdisciplinary connections with " + topic
        );
    }
}
