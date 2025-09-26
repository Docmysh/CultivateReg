// src/main/java/com/bo/cultivatereg/cultivation/MobCultivationEvents.java
package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.config.ModConfigs;
import com.bo.cultivatereg.network.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class MobCultivationEvents {

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent e) {
        if (e.getLevel().isClientSide()) return;

        Entity ent = e.getEntity();
        if (!(ent instanceof LivingEntity le) || le instanceof Player) return;

        le.getCapability(MobCultivationCapability.CAP).ifPresent(data -> {
            // Randomize once if not set yet (persisted via capability NBT)
            if (!data.hasCultivation()) {
                RandomSource r = le.getRandom();

                // Config-driven chance & weights
                double chance = ModConfigs.COMMON.cultivatedSpawnChance.get();
                if (r.nextDouble() < chance) {
                    int wQi   = ModConfigs.COMMON.qiGatheringWeight.get();
                    int wFou  = ModConfigs.COMMON.foundationWeight.get();
                    int wCore = ModConfigs.COMMON.coreWeight.get();
                    int total = Math.max(1, wQi + wFou + wCore);
                    int pick  = r.nextInt(total);

                    Realm realm;
                    if ((pick -= wQi) < 0) realm = Realm.QI_GATHERING;
                    else if ((pick -= wFou) < 0) realm = Realm.FOUNDATION;
                    else realm = Realm.CORE_FORMATION;

                    int stage = 1 + r.nextInt(9);
                    data.setHasCultivation(true);
                    data.setRealm(realm);
                    data.setStage(stage);
                } else {
                    data.setHasCultivation(false);
                    data.setRealm(Realm.MORTAL);
                    data.setStage(1);
                }
            }

            // Always reapply attributes (covers saved mobs, chunk reloads, etc.)
            MobCultivationAttributes.apply(le, data);

            // Add flight/chase logic for Foundation+ (once per entity)
            if (data.hasCultivation()
                    && data.getRealm().ordinal() >= Realm.FOUNDATION.ordinal()
                    && !le.getPersistentData().getBoolean("cr_flight_goal")) {

                if (le instanceof Creeper c) {
                    // Remove vanilla swell goal so our custom flying/ignite goal fully controls behavior
                    for (WrappedGoal wg : c.goalSelector.getAvailableGoals().toArray(new WrappedGoal[0])) {
                        Goal g = wg.getGoal();
                        if (g.getClass().getName().contains("Creeper$SwellGoal")) {
                            c.goalSelector.removeGoal(g);
                        }
                    }
                    double accel = 0.08; // snappy hover
                    double maxSpd = (data.getRealm() == Realm.CORE_FORMATION) ? 0.60 : 0.45;
                    c.goalSelector.addGoal(1, new QiFlightCreeperGoal(c, accel, maxSpd));
                } else if (le instanceof PathfinderMob pm) {
                    // General flying melee pursuit for other mobs (zombie, etc.)
                    double accel = (data.getRealm() == Realm.CORE_FORMATION) ? 0.08 : 0.05;
                    double maxSpd = (data.getRealm() == Realm.CORE_FORMATION) ? 0.60 : 0.45;
                    int atkCD     = (data.getRealm() == Realm.CORE_FORMATION) ? 14   : 18;
                    pm.goalSelector.addGoal(2, new QiFlightMeleeGoal(pm, accel, maxSpd, atkCD));
                }

                le.getPersistentData().putBoolean("cr_flight_goal", true);
            }
        });
    }

    /**
     * Scale creeper explosions by cultivation.
     * We cancel the vanilla explosion and trigger our own with the configured radius.
     */
    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start e) {
        Level level = e.getLevel();
        if (level.isClientSide()) return;

        var explosion = e.getExplosion();
        Entity exploder = explosion.getExploder();
        if (!(exploder instanceof Creeper c)) return;

        c.getCapability(MobCultivationCapability.CAP).ifPresent(data -> {
            // Compute desired radius from config & cultivation
            float radius = configuredCreeperRadius(data);

            // Charged creepers still get their vanilla x2 power
            if (c.isPowered()) radius *= 2.0f;

            // Replace the vanilla explosion with our own radius
            e.setCanceled(true);
            // Use MOB interaction (same as creeper), no fire by default; adjust if you want flaming blasts
            level.explode(
                    c,                      // source entity
                    c.getX(), c.getY(), c.getZ(),
                    radius,
                    Level.ExplosionInteraction.MOB
            );
            // Vanilla explodeCreeper() kills the creeper afterwards; mimic that.
            c.discard();
        });
    }

    private static float configuredCreeperRadius(MobCultivationData data) {
        double base = ModConfigs.COMMON.creeperBaseRadius.get(); // vanilla ~3.0
        if (!data.hasCultivation()) return (float) base;

        // Qi: base x5 +1 per stage, Foundation: x10, Core: x20 (all config-driven)
        double qiScale = ModConfigs.COMMON.creeperQiBaseMult.get()
                + ModConfigs.COMMON.creeperPerStageAddMult.get() * (data.getStage() - 1);

        double realmScale = switch (data.getRealm()) {
            case QI_GATHERING   -> 1.0;
            case FOUNDATION     -> ModConfigs.COMMON.creeperFoundationRealmMult.get();
            case CORE_FORMATION -> ModConfigs.COMMON.creeperCoreRealmMult.get();
            default             -> 1.0;
        };

        return (float) (base * qiScale * realmScale);
    }

    // Ensure newly tracking players get a client copy of the mob's cultivation (for nameplates, etc.)
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        if (!(e.getTarget() instanceof LivingEntity le)) return;
        le.getCapability(MobCultivationCapability.CAP).ifPresent(data ->
                Net.syncMobToPlayer(sp, le, data));
    }
}
