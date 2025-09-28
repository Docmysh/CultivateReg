package com.bo.cultivatereg.cultivation.manual;

import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Represents a cultivation manual that defines how far a player can progress while following it.
 */
public final class CultivationManual {
    private final ResourceLocation id;
    private final String displayName;
    private final String description;
    private final String content;
    private final String breakthroughRequirement;
    private final Realm prerequisiteRealm;
    private final Realm targetRealm;
    private final int maxStage;
    private final List<ManualQuestion> quiz;

    public CultivationManual(ResourceLocation id, String displayName,
                             String description, String content, String breakthroughRequirement,
                             Realm prerequisiteRealm, Realm targetRealm,
                             int maxStage, List<ManualQuestion> quiz) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = Objects.requireNonNull(description, "description");
        this.content = Objects.requireNonNull(content, "content");
        this.breakthroughRequirement = Objects.requireNonNull(breakthroughRequirement, "breakthroughRequirement");
        this.prerequisiteRealm = Objects.requireNonNull(prerequisiteRealm, "prerequisiteRealm");
        this.targetRealm = Objects.requireNonNull(targetRealm, "targetRealm");
        this.maxStage = maxStage;
        this.quiz = List.copyOf(Objects.requireNonNull(quiz, "quiz"));
    }

    public ResourceLocation id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String content() {
        return content;
    }

    public String breakthroughRequirement() {
        return breakthroughRequirement;
    }

    public Realm prerequisiteRealm() {
        return prerequisiteRealm;
    }

    public Realm targetRealm() {
        return targetRealm;
    }

    /** Maximum stage (inclusive) attainable within {@link #targetRealm()} while using this manual. */
    public OptionalInt stageCapFor(Realm realm) {
        return realm == targetRealm ? OptionalInt.of(maxStage) : OptionalInt.empty();
    }

    public List<ManualQuestion> quiz() {
        return quiz;
    }

    /** Whether the manual allows a breakthrough from the given realm. */
    public boolean canBreakthroughFrom(Realm currentRealm) {
        return currentRealm == prerequisiteRealm;
    }
}