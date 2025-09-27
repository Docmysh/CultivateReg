// src/main/java/com/bo/cultivatereg/config/ModConfigs.java
package com.bo.cultivatereg.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigs {
    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        COMMON = new Common(b);
        SPEC = b.build();
    }

    public static class Common {
        // --- Spawn & distribution ---
        public final ForgeConfigSpec.DoubleValue cultivatedSpawnChance; // 0..1
        public final ForgeConfigSpec.IntValue qiGatheringWeight;
        public final ForgeConfigSpec.IntValue foundationWeight;
        public final ForgeConfigSpec.IntValue coreWeight;
        public final ForgeConfigSpec.IntValue nascentSoulWeight;
        public final ForgeConfigSpec.IntValue soulTransformationWeight;
        public final ForgeConfigSpec.IntValue spiritSeveringWeight;
        public final ForgeConfigSpec.IntValue voidRefiningWeight;
        public final ForgeConfigSpec.IntValue integrationWeight;
        public final ForgeConfigSpec.IntValue tribulationWeight;

        // --- Attribute scaling (multipliers on base attributes) ---
        public final ForgeConfigSpec.DoubleValue qiHealthBase;
        public final ForgeConfigSpec.DoubleValue foundationHealthBase;
        public final ForgeConfigSpec.DoubleValue coreHealthBase;
        public final ForgeConfigSpec.DoubleValue perStageHealthAdd;

        public final ForgeConfigSpec.DoubleValue qiDamageBase;
        public final ForgeConfigSpec.DoubleValue foundationDamageBase;
        public final ForgeConfigSpec.DoubleValue coreDamageBase;
        public final ForgeConfigSpec.DoubleValue perStageDamageAdd;

        public final ForgeConfigSpec.DoubleValue qiSpeedBase;
        public final ForgeConfigSpec.DoubleValue foundationSpeedBase;
        public final ForgeConfigSpec.DoubleValue coreSpeedBase;
        public final ForgeConfigSpec.DoubleValue perStageSpeedAdd;

        // --- Heavenly Sword skill ---
        public final ForgeConfigSpec.DoubleValue heavenlySwordCost;          // Spirit cost
        public final ForgeConfigSpec.IntValue    heavenlySwordCooldown;      // ticks
        public final ForgeConfigSpec.IntValue    heavenlySwordWarmupTicks;   // pre-roll (can keep for other uses)
        public final ForgeConfigSpec.DoubleValue heavenlySwordSpawnHeight;   // blocks above target (legacy)
        public final ForgeConfigSpec.DoubleValue heavenlySwordFallSpeed;     // blocks/tick
        public final ForgeConfigSpec.DoubleValue heavenlySwordRadius;        // AoE radius
        public final ForgeConfigSpec.DoubleValue heavenlySwordBaseDamage;
        public final ForgeConfigSpec.DoubleValue heavenlySwordPerStage;
        public final ForgeConfigSpec.DoubleValue heavenlySwordQiMult;
        public final ForgeConfigSpec.DoubleValue heavenlySwordFoundationMult;
        public final ForgeConfigSpec.DoubleValue heavenlySwordCoreMult;

        // NEW: used by StartHeavenlySwordChargePacket/skill
        public final ForgeConfigSpec.IntValue    heavenlySwordChargeTicks;   // how long the charge can last
        public final ForgeConfigSpec.DoubleValue heavenlySwordHover;         // hover height above portal plane

        // Targeting/visuals (you already had these — keeping them)
        public final ForgeConfigSpec.DoubleValue heavenlySwordAimDistance;   // blocks in front of player
        public final ForgeConfigSpec.DoubleValue heavenlySwordAimHeight;     // portal Y offset from eye
        public final ForgeConfigSpec.DoubleValue heavenlySwordHoverHeight;   // legacy/alt hover height
        public final ForgeConfigSpec.DoubleValue heavenlySwordScale;         // visual scale of sword

        // --- Creeper explosions ---
        public final ForgeConfigSpec.DoubleValue creeperBaseRadius;
        public final ForgeConfigSpec.DoubleValue creeperQiBaseMult;
        public final ForgeConfigSpec.DoubleValue creeperPerStageAddMult;
        public final ForgeConfigSpec.DoubleValue creeperFoundationRealmMult;
        public final ForgeConfigSpec.DoubleValue creeperCoreRealmMult;
        public final ForgeConfigSpec.DoubleValue creeperAdvancedRealmStep;

        public Common(ForgeConfigSpec.Builder b) {
            b.push("cultivation_mobs");
            cultivatedSpawnChance = b.comment("Chance (0..1) a natural mob rolls cultivation")
                    .defineInRange("cultivatedSpawnChance", 0.25d, 0d, 1d);
            qiGatheringWeight = b.defineInRange("qiGatheringWeight", 80, 0, 1000);
            foundationWeight  = b.defineInRange("foundationWeight", 18, 0, 1000);
            coreWeight        = b.defineInRange("coreWeight", 2,  0, 1000);
            nascentSoulWeight = b.defineInRange("nascentSoulWeight", 1, 0, 1000);
            soulTransformationWeight = b.defineInRange("soulTransformationWeight", 1, 0, 1000);
            spiritSeveringWeight = b.defineInRange("spiritSeveringWeight", 1, 0, 1000);
            voidRefiningWeight = b.defineInRange("voidRefiningWeight", 1, 0, 1000);
            integrationWeight = b.defineInRange("integrationWeight", 1, 0, 1000);
            tribulationWeight = b.defineInRange("tribulationWeight", 1, 0, 1000);
            b.pop();

            b.push("attributes_health");
            qiHealthBase         = b.defineInRange("qiHealthBase",         1.50d, 0d, 100d);
            foundationHealthBase = b.defineInRange("foundationHealthBase", 2.50d, 0d, 100d);
            coreHealthBase       = b.defineInRange("coreHealthBase",       4.00d, 0d, 100d);
            perStageHealthAdd    = b.comment("+ per stage (0.10 = +10%)")
                    .defineInRange("perStageHealthAdd", 0.10d, 0d, 10d);
            b.pop();

            b.push("attributes_damage");
            qiDamageBase         = b.defineInRange("qiDamageBase",         1.25d, 0d, 100d);
            foundationDamageBase = b.defineInRange("foundationDamageBase", 2.00d, 0d, 100d);
            coreDamageBase       = b.defineInRange("coreDamageBase",       3.00d, 0d, 100d);
            perStageDamageAdd    = b.defineInRange("perStageDamageAdd",    0.08d, 0d, 10d);
            b.pop();

            b.push("attributes_speed");
            qiSpeedBase          = b.defineInRange("qiSpeedBase",          1.10d, 0d, 10d);
            foundationSpeedBase  = b.defineInRange("foundationSpeedBase",  1.20d, 0d, 10d);
            coreSpeedBase        = b.defineInRange("coreSpeedBase",        1.30d, 0d, 10d);
            perStageSpeedAdd     = b.defineInRange("perStageSpeedAdd",     0.01d, 0d, 1d);
            b.pop();

            b.push("creeper_explosions");
            creeperBaseRadius          = b.defineInRange("creeperBaseRadius",           3.0d, 0d, 100d);
            creeperQiBaseMult          = b.defineInRange("creeperQiBaseMult",           5.0d, 0d, 100d);
            creeperPerStageAddMult     = b.defineInRange("creeperPerStageAddMult",      1.0d, 0d, 100d);
            creeperFoundationRealmMult = b.defineInRange("creeperFoundationRealmMult", 10.0d, 0d, 100d);
            creeperCoreRealmMult       = b.defineInRange("creeperCoreRealmMult",       20.0d, 0d, 100d);
            creeperAdvancedRealmStep   = b.comment("Multiplier applied per realm above Core Formation")
                    .defineInRange("creeperAdvancedRealmStep", 1.5d, 1.0d, 100d);
            b.pop();

            b.push("heavenly_sword");
            heavenlySwordCost           = b.defineInRange("costSpirit",      25.0d, 0d, 10000d);
            heavenlySwordCooldown       = b.defineInRange("cooldownTicks",   160,   0, 20*60*10);
            heavenlySwordWarmupTicks    = b.defineInRange("warmupTicks",     20,    0, 20*10);
            heavenlySwordSpawnHeight    = b.defineInRange("spawnHeight",     28.0d, 4.0d, 256.0d);
            heavenlySwordFallSpeed      = b.defineInRange("fallSpeed",       1.5d,  0.1d, 20.0d);
            heavenlySwordRadius         = b.defineInRange("radius",          3.5d,  0.5d, 32.0d);
            heavenlySwordBaseDamage     = b.defineInRange("baseDamage",      10.0d, 0d, 10000d);
            heavenlySwordPerStage       = b.defineInRange("perStage",        2.0d,  0d, 1000d);
            heavenlySwordQiMult         = b.defineInRange("qiGatherMult",    1.0d,  0d, 100d);
            heavenlySwordFoundationMult = b.defineInRange("foundationMult",  2.0d,  0d, 100d);
            heavenlySwordCoreMult       = b.defineInRange("coreMult",        3.5d,  0d, 100d);

            // NEW
            heavenlySwordChargeTicks    = b.defineInRange("chargeTicks",     40,    1, 20*60*10);
            b.pop();

            b.push("heavenly_sword_targeting");
            heavenlySwordAimDistance = b.defineInRange("aimDistance", 12.0d, 1d,   256d);
            heavenlySwordAimHeight   = b.defineInRange("aimHeight",    8.0d, -64d, 256d);
            heavenlySwordHoverHeight = b.defineInRange("hoverHeight", 16.0d, 0d,   256d);
            heavenlySwordScale       = b.defineInRange("scale",       30.0d, 1d,   200d);

            // NEW (name used by your packets)
            heavenlySwordHover       = b.defineInRange("hover",       18.0d, 0d,   256d);
            b.pop();
        }
    }
}
