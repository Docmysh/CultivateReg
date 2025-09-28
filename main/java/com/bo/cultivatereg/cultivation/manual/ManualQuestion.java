package com.bo.cultivatereg.cultivation.manual;

import java.util.List;

/**
 * Represents a simple multi-choice question used when studying a manual.
 */
public record ManualQuestion(String prompt, List<String> options, int correctIndex) {
    public ManualQuestion {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Question prompt cannot be empty");
        }
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Question must provide at least one option");
        }
        if (correctIndex < 0 || correctIndex >= options.size()) {
            throw new IllegalArgumentException("Correct answer index out of range");
        }
    }
}