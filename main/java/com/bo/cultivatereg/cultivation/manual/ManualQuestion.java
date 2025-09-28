package com.bo.cultivatereg.cultivation.manual;

import java.util.List;
import java.util.Set;

/**
 * Represents a simple multi-choice question used when studying a manual.
 */
public record ManualQuestion(String prompt, List<String> options, Set<Integer> correctIndices) {
    public ManualQuestion {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Question prompt cannot be empty");
        }
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Question must provide at least one option");
        }
        options = List.copyOf(options);
        if (correctIndices == null || correctIndices.isEmpty()) {
            throw new IllegalArgumentException("Question must specify at least one correct answer");
        }
        correctIndices = Set.copyOf(correctIndices);
        int optionCount = options.size();
        for (int index : correctIndices) {
            if (index < 0 || index >= optionCount) {
                throw new IllegalArgumentException("Correct answer index out of range: " + index);
            }
        }
    }
}