package com.bo.cultivatereg.cultivation;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class QiFlightMeleeGoal extends Goal {
    private final PathfinderMob mob;
    private final double accel;
    private final double maxSpeed;
    private final int attackCooldownTicks;

    private LivingEntity target;
    private int attackCooldown;

    public QiFlightMeleeGoal(PathfinderMob mob, double accel, double maxSpeed, int attackCooldownTicks) {
        this.mob = mob;
        this.accel = accel;
        this.maxSpeed = maxSpeed;
        this.attackCooldownTicks = attackCooldownTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        return mob.getCapability(MobCultivationCapability.CAP)
                .map(d -> d.hasCultivation() && d.getRealm().ordinal() >= Realm.FOUNDATION.ordinal())
                .orElse(false);
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && canUse();
    }

    @Override
    public void start() {
        mob.setNoGravity(true);
        attackCooldown = 10;
        // Stop pathfinding fighting our steering
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        mob.setNoGravity(false);
        target = null;
        mob.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (target == null) return;

        // steer toward target (simple homing)
        Vec3 to = new Vec3(
                target.getX() - mob.getX(),
                (target.getY() + target.getBbHeight() * 0.5) - (mob.getY() + mob.getBbHeight() * 0.5),
                target.getZ() - mob.getZ()
        );
        Vec3 dir = to.lengthSqr() > 1.0e-6 ? to.normalize() : Vec3.ZERO;
        Vec3 vel = mob.getDeltaMovement().scale(0.85).add(dir.scale(accel));
        if (vel.lengthSqr() > maxSpeed * maxSpeed) vel = vel.normalize().scale(maxSpeed);
        mob.setDeltaMovement(vel);
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // melee check
        if (attackCooldown > 0) attackCooldown--;
        double reach = mob.getBbWidth() * 1.8 + target.getBbWidth(); // close to vanilla melee reach
        if (mob.distanceToSqr(target) <= reach * reach && mob.hasLineOfSight(target)) {
            if (attackCooldown <= 0) {
                mob.swing(InteractionHand.MAIN_HAND);
                mob.doHurtTarget(target); // deals damage using ATTACK_DAMAGE attribute
                attackCooldown = attackCooldownTicks;
            }
        }
    }
}
