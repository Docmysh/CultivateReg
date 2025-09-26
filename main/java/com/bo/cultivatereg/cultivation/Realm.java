package com.bo.cultivatereg.cultivation;

/**
 * Major realms only. Minor realms (stages 1..9) are tracked separately.
 */
public enum Realm {
    MORTAL(0f),
    QI_GATHERING(0.50f),
    FOUNDATION(0.30f),
    CORE_FORMATION(0.10f);

    /** Base Qi per tick while meditating in this realm (before stage scaling). */
    public final float baseRate;

    Realm(float baseRate) {
        this.baseRate = baseRate;
    }

    public Realm nextMajor() {
        return switch (this) {
            case MORTAL -> QI_GATHERING;
            case QI_GATHERING -> FOUNDATION;
            case FOUNDATION -> CORE_FORMATION;
            case CORE_FORMATION -> CORE_FORMATION;
        };
    }

    /** Cap (Qi needed) to advance from the given stage to the next stage/realm. Stages: 1..9. */
    public int capForStage(int stage) {
        if (this == MORTAL) return 0;
        stage = Math.max(1, Math.min(9, stage));
        // Simple escalating caps; tune later or move to config.
        return switch (this) {
            case QI_GATHERING -> switch (stage) {
                case 1 -> 100;  case 2 -> 180;  case 3 -> 270;
                case 4 -> 380;  case 5 -> 510;  case 6 -> 660;
                case 7 -> 830;  case 8 -> 1020; case 9 -> 1230;
                default -> 100;
            };
            case FOUNDATION -> switch (stage) {
                case 1 -> 800;  case 2 -> 1000; case 3 -> 1250;
                case 4 -> 1550; case 5 -> 1900; case 6 -> 2300;
                case 7 -> 2750; case 8 -> 3250; case 9 -> 3800;
                default -> 800;
            };
            case CORE_FORMATION -> switch (stage) {
                case 1 -> 3000; case 2 -> 3500; case 3 -> 4100;
                case 4 -> 4800; case 5 -> 5600; case 6 -> 6500;
                case 7 -> 7500; case 8 -> 8600; case 9 -> 9800;
                default -> 3000;
            };
            default -> 0;
        };
    }

    /** Stage multiplier for Qi rate (small bump per stage). */
    public float rateMultiplierForStage(int stage) {
        if (this == MORTAL) return 0f;
        stage = Math.max(1, Math.min(9, stage));
        return 1.0f + 0.06f * (stage - 1); // +6% per stage
    }
}
