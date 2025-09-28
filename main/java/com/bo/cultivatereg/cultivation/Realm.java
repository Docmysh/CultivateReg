package com.bo.cultivatereg.cultivation;

/**
 * Major realms only. Minor realms (stages 1..9) are tracked separately.
 *
 * <p>The enum now centralises a bunch of realm specific tuning so that adding
 * extra realms does not require touching half a dozen switch statements
 * throughout the code base. Values were chosen to preserve the existing tuning
 * for the original four realms while extending naturally for the additional
 * tiers.</p>
 */
public enum Realm {
    MORTAL(
            Tier.MORTAL,
            0.00f,
            "Mortal",
            0xFFC0C0C0,
            new float[]{0.60f, 0.60f, 0.60f},
            0,
            0,
            new int[0]
    ),
    QI_GATHERING(
            Tier.QI,
            0.18f,
            "Qi Gather",
            0xFF4FC3F7,
            new float[]{0.30f, 0.85f, 1.00f},
            140,
            25,
            new int[]{100, 180, 270, 380, 510, 660, 830, 1020, 1230}
    ),
    FOUNDATION(
            Tier.FOUNDATION,
            0.10f,
            "Foundation",
            0xFFB388FF,
            new float[]{0.70f, 0.40f, 1.00f},
            320,
            40,
            new int[]{800, 1000, 1250, 1550, 1900, 2300, 2750, 3250, 3800}
    ),
    CORE_FORMATION(
            Tier.CORE,
            0.06f,
            "Core Form",
            0xFFFFD54F,
            new float[]{1.00f, 0.85f, 0.20f},
            600,
            90,
            new int[]{3000, 3500, 4100, 4800, 5600, 6500, 7500, 8600, 9800}
    ),
    NASCENT_SOUL(
            Tier.CORE,
            0.04f,
            "Nascent",
            0xFFFFA726,
            new float[]{1.00f, 0.55f, 0.25f},
            800,
            120,
            new int[]{13000, 14800, 16800, 19000, 21400, 24000, 26800, 29800, 33000}
    ),
    SOUL_TRANSFORMATION(
            Tier.CORE,
            0.028f,
            "Soul Trans",
            0xFFFF7043,
            new float[]{0.95f, 0.35f, 0.40f},
            1050,
            150,
            new int[]{36000, 38600, 41400, 44400, 47600, 51000, 54600, 58400, 62400}
    ),
    SPIRIT_SEVERING(
            Tier.CORE,
            0.020f,
            "Spirit Sev",
            0xFFE573AB,
            new float[]{0.90f, 0.30f, 0.60f},
            1350,
            180,
            new int[]{66000, 69300, 72800, 76500, 80400, 84500, 88800, 93300, 98000}
    ),
    VOID_REFINING(
            Tier.CORE,
            0.014f,
            "Void Ref",
            0xFF64B5F6,
            new float[]{0.55f, 0.70f, 1.00f},
            1700,
            210,
            new int[]{103000, 107000, 111000, 115000, 119000, 123000, 127000, 131000, 135000}
    ),
    INTEGRATION(
            Tier.CORE,
            0.010f,
            "Integration",
            0xFF26C6DA,
            new float[]{0.30f, 0.90f, 0.75f},
            2100,
            240,
            new int[]{140000, 145000, 150000, 155000, 160000, 165000, 170000, 175000, 180000}
    ),
    TRIBULATION(
            Tier.CORE,
            0.007f,
            "Tribulation",
            0xFFF5F5F5,
            new float[]{0.95f, 0.95f, 1.00f},
            2550,
            270,
            new int[]{186000, 192000, 198000, 204000, 210000, 216000, 222000, 228000, 234000}
    );

    private static final Realm[] VALUES = values();

    /** Base Qi per tick while meditating in this realm (before stage scaling). */
    public final float baseRate;

    private final Tier tier;
    private final String shortName;
    private final int nameplateColor;
    private final float[] shieldColor;
    private final int spiritBase;
    private final int spiritPerStage;
    private final int[] stageCaps;

    Realm(Tier tier, float baseRate, String shortName, int nameplateColor,
          float[] shieldColor, int spiritBase, int spiritPerStage, int[] stageCaps) {
        this.tier = tier;
        this.baseRate = baseRate;
        this.shortName = shortName;
        this.nameplateColor = nameplateColor;
        this.shieldColor = shieldColor.clone();
        this.spiritBase = spiritBase;
        this.spiritPerStage = spiritPerStage;
        this.stageCaps = stageCaps.clone();
    }

    public Realm nextMajor() {
        int idx = ordinal();
        return (idx >= VALUES.length - 1) ? this : VALUES[idx + 1];
    }

    /** Cap (Qi needed) to advance from the given stage to the next stage/realm. Stages: 1..9. */
    public int capForStage(int stage) {
        if (this == MORTAL || stageCaps.length == 0) return 0;
        int idx = clampStage(stage) - 1;
        return stageCaps[idx];
    }

    /** Stage multiplier for Qi rate (small bump per stage). */
    public float rateMultiplierForStage(int stage) {
        if (this == MORTAL) return 0f;
        return 1.0f + 0.06f * (clampStage(stage) - 1);
    }

    public int spiritCapForStage(int stage) {
        if (this == MORTAL) return 0;
        int s = clampStage(stage);
        if (spiritPerStage <= 0) return Math.max(0, spiritBase);
        return Math.max(0, spiritBase + spiritPerStage * (s - 1));
    }

    public float spiritRegenPerSecond() {
        return tier.spiritRegenPerSecond;
    }

    public float healthRegenPerSecond() {
        return tier.healthRegenPerSecond;
    }

    public double mobPowerMultiplier(int stage) {
        return tier.mobPowerMultiplier(clampStage(stage));
    }

    public double mobArmorBonus(int stage) {
        return tier.mobArmorBonus(clampStage(stage));
    }

    public PlayerModifiers playerModifiers(int stage) {
        return tier.playerModifiers(clampStage(stage));
    }

    public String shortName() {
        return shortName;
    }

    public int nameplateColor() {
        return nameplateColor;
    }

    public float[] shieldColor() {
        return shieldColor.clone();
    }

    public boolean isCoreTier() {
        return tier == Tier.CORE;
    }

    private static int clampStage(int stage) {
        return Math.max(1, Math.min(9, stage));
    }

    private enum Tier {
        MORTAL(
                0.0f, 0.5f,
                0.0, 0.0,
                1.00, 0.00,
                PlayerScaling.ZERO, PlayerScaling.ZERO, PlayerScaling.ZERO, PlayerScaling.ZERO
        ),
        QI(
                .05f, 0.8f,
                2.0, 0.2,
                1.20, 0.05,
                new PlayerScaling(2.0, 2.0),
                new PlayerScaling(0.02, 0.02),
                new PlayerScaling(0.5, 0.5),
                new PlayerScaling(0.02, 0.02)
        ),
        FOUNDATION(
                0.06f, 1.2f,
                6.0, 0.4,
                1.80, 0.05,
                new PlayerScaling(22.0, 2.0),
                new PlayerScaling(0.08, 0.03),
                new PlayerScaling(2.75, 0.75),
                new PlayerScaling(0.08, 0.03)
        ),
        CORE(
                0.08f, 1.6f,
                12.0, 0.6,
                3.00, 0.05,
                new PlayerScaling(44.0, 4.0),
                new PlayerScaling(0.15, 0.05),
                new PlayerScaling(6.0, 1.0),
                new PlayerScaling(0.15, 0.05)
        );

        private final float spiritRegenPerSecond;
        private final float healthRegenPerSecond;
        private final double mobArmorBase;
        private final double mobArmorPerStage;
        private final double mobPowerBase;
        private final double mobPowerStageBonus;
        private final PlayerScaling hpAdd;
        private final PlayerScaling speedMul;
        private final PlayerScaling dmgAdd;
        private final PlayerScaling kbMul;

        Tier(float spiritRegenPerSecond, float healthRegenPerSecond,
             double mobArmorBase, double mobArmorPerStage,
             double mobPowerBase, double mobPowerStageBonus,
             PlayerScaling hpAdd, PlayerScaling speedMul,
             PlayerScaling dmgAdd, PlayerScaling kbMul) {
            this.spiritRegenPerSecond = spiritRegenPerSecond;
            this.healthRegenPerSecond = healthRegenPerSecond;
            this.mobArmorBase = mobArmorBase;
            this.mobArmorPerStage = mobArmorPerStage;
            this.mobPowerBase = mobPowerBase;
            this.mobPowerStageBonus = mobPowerStageBonus;
            this.hpAdd = hpAdd;
            this.speedMul = speedMul;
            this.dmgAdd = dmgAdd;
            this.kbMul = kbMul;
        }

        private double mobArmorBonus(int stage) {
            if (stage <= 0) return 0.0;
            return mobArmorBase + mobArmorPerStage * (stage - 1);
        }

        private double mobPowerMultiplier(int stage) {
            if (stage <= 0) return mobPowerBase;
            return mobPowerBase * (1.0 + mobPowerStageBonus * Math.max(0, stage - 1));
        }

        private PlayerModifiers playerModifiers(int stage) {
            if (stage <= 0) {
                return new PlayerModifiers(0.0, 0.0, 0.0, 0.0);
            }
            return new PlayerModifiers(
                    hpAdd.value(stage),
                    speedMul.value(stage),
                    dmgAdd.value(stage),
                    kbMul.value(stage)
            );
        }
    }

    private static final class PlayerScaling {
        static final PlayerScaling ZERO = new PlayerScaling(0.0, 0.0);

        private final double base;
        private final double perStage;

        private PlayerScaling(double base, double perStage) {
            this.base = base;
            this.perStage = perStage;
        }

        double value(int stage) {
            if (stage <= 0) {
                return 0.0;
            }
            return base + perStage * Math.max(0, stage - 1);
        }
    }

    public static final class PlayerModifiers {
        private final double hpAdd;
        private final double speedMul;
        private final double dmgAdd;
        private final double kbMul;

        public PlayerModifiers(double hpAdd, double speedMul, double dmgAdd, double kbMul) {
            this.hpAdd = hpAdd;
            this.speedMul = speedMul;
            this.dmgAdd = dmgAdd;
            this.kbMul = kbMul;
        }

        public double hpAdd() {
            return hpAdd;
        }

        public double speedMul() {
            return speedMul;
        }

        public double dmgAdd() {
            return dmgAdd;
        }

        public double kbMul() {
            return kbMul;
        }
    }
}
