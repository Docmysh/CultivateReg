// src/main/java/com/bo/cultivatereg/cultivation/QiFlightCreeperGoal.java
package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.config.ModConfigs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class QiFlightCreeperGoal extends Goal {
    private final Creeper creeper;
    private final double accel;
    private final double maxSpeed;
    private LivingEntity target;

    // Our custom fuse (vanilla max is 30 ticks)
    private static final int MAX_FUSE = 30;
    private int fuseTicks = 0;
    private boolean primedSoundPlayed = false;

    // how close before we start “priming”
    private static final double TRIGGER_RANGE_SQR = 3.0 * 3.0;

    public QiFlightCreeperGoal(Creeper creeper, double accel, double maxSpeed) {
        this.creeper = creeper;
        this.accel = accel;
        this.maxSpeed = maxSpeed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        target = creeper.getTarget();
        if (target == null || !target.isAlive()) return false;

        // Only for cultivated Foundation+
        return creeper.getCapability(MobCultivationCapability.CAP)
                .map(d -> d.hasCultivation() && d.getRealm().ordinal() >= Realm.FOUNDATION.ordinal())
                .orElse(false);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && canUse();
    }

    @Override
    public void start() {
        creeper.setNoGravity(true);
        creeper.getNavigation().stop();
        fuseTicks = 0;
        primedSoundPlayed = false;
        // do NOT call creeper.setSwellDir(1) — we run a custom fuse/boom
    }

    @Override
    public void stop() {
        creeper.setNoGravity(false);
        target = null;
        fuseTicks = 0;
        primedSoundPlayed = false;
        creeper.setDeltaMovement(Vec3.ZERO);
        // ensure vanilla fuse is reset as well
        creeper.setSwellDir(-1);
    }

    @Override
    public void tick() {
        if (target == null) return;

        // steer/hover toward target
        Vec3 to = new Vec3(
                target.getX() - creeper.getX(),
                (target.getY() + target.getBbHeight() * 0.5) - (creeper.getY() + creeper.getBbHeight() * 0.5),
                target.getZ() - creeper.getZ()
        );
        double dist2 = to.lengthSqr();
        Vec3 dir = dist2 > 1.0e-6 ? to.normalize() : Vec3.ZERO;

        Vec3 vel = creeper.getDeltaMovement().scale(0.85).add(dir.scale(accel));
        if (vel.lengthSqr() > maxSpeed * maxSpeed) vel = vel.normalize().scale(maxSpeed);
        creeper.setDeltaMovement(vel);
        creeper.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // priming zone (close & LOS)
        boolean arming = dist2 <= TRIGGER_RANGE_SQR && creeper.hasLineOfSight(target);

        if (arming) {
            if (!primedSoundPlayed && !creeper.level().isClientSide) {
                creeper.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 1.0F);
                primedSoundPlayed = true;
            }
            // slow drift so it doesn't push away
            creeper.setDeltaMovement(creeper.getDeltaMovement().scale(0.4));
            fuseTicks = Math.min(MAX_FUSE, fuseTicks + 1);
        } else {
            primedSoundPlayed = false;
            fuseTicks = Math.max(0, fuseTicks - 2);
        }

        // explode (server only)
        if (fuseTicks >= MAX_FUSE && !creeper.level().isClientSide) {
            float radius = computeExplosionRadiusFromCap();
            boolean grief = creeper.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            Level.ExplosionInteraction mode = grief ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE;

            creeper.level().explode(
                    creeper,                   // source entity
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    radius,
                    mode
            );
            creeper.discard(); // remove the creeper after the custom boom
        }
    }

    private float computeExplosionRadiusFromCap() {
        var cap = creeper.getCapability(MobCultivationCapability.CAP).resolve().orElse(null);
        var C = ModConfigs.COMMON;

        // Unbox to double (no invalid cast), then cast to float only at return.
        double base = C.creeperBaseRadius.get(); // Double -> double

        if (cap == null || !cap.hasCultivation()) {
            return (float) base;
        }

        int stage = Math.max(1, Math.min(9, cap.getStage()));

        double qiBase   = C.creeperQiBaseMult.get();
        double perStage = C.creeperPerStageAddMult.get();
        double foundMul = C.creeperFoundationRealmMult.get();
        double coreMul  = C.creeperCoreRealmMult.get();

        double qiScaled = qiBase + perStage * Math.max(0, stage - 1);

        double mult = switch (cap.getRealm()) {
            case MORTAL -> 1.0;
            case QI_GATHERING -> qiScaled;
            case FOUNDATION -> qiScaled * foundMul;
            case CORE_FORMATION -> qiScaled * coreMul;
        };

        double radius = Math.max(1.0, Math.min(256.0, base * mult));
        return (float) radius;
    }

}
