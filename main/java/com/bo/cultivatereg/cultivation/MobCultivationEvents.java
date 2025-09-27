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
                    int[] weights = new int[]{
                            ModConfigs.COMMON.qiGatheringWeight.get(),
                            ModConfigs.COMMON.foundationWeight.get(),
                            ModConfigs.COMMON.coreWeight.get(),
                            ModConfigs.COMMON.nascentSoulWeight.get(),
                            ModConfigs.COMMON.soulTransformationWeight.get(),
                            ModConfigs.COMMON.spiritSeveringWeight.get(),
                            ModConfigs.COMMON.voidRefiningWeight.get(),
                            ModConfigs.COMMON.integrationWeight.get(),
                            ModConfigs.COMMON.tribulationWeight.get()
                    };
                    Realm[] realms = new Realm[]{
                            Realm.QI_GATHERING,
                            Realm.FOUNDATION,
                            Realm.CORE_FORMATION,
                            Realm.NASCENT_SOUL,
                            Realm.SOUL_TRANSFORMATION,
                            Realm.SPIRIT_SEVERING,
                            Realm.VOID_REFINING,
                            Realm.INTEGRATION,
                            Realm.TRIBULATION
                    };

                    int total = 0;
                    for (int w : weights) total += Math.max(0, w);

                    Realm realm = Realm.QI_GATHERING;
                    if (total > 0) {
                        int pick = r.nextInt(total);
                        for (int i = 0; i < weights.length; i++) {
                            pick -= Math.max(0, weights[i]);
                            if (pick < 0) {
                                realm = realms[i];
                                break;
                            }
                        }
                    }

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
                    int tier = Math.max(0, data.getRealm().ordinal() - Realm.FOUNDATION.ordinal());
                    double accel = 0.05 + 0.03 * tier;
                    double maxSpd = 0.45 + 0.15 * tier;
                    c.goalSelector.addGoal(1, new QiFlightCreeperGoal(c, Math.min(accel, 0.20), Math.min(maxSpd, 1.50)));
                } else if (le instanceof PathfinderMob pm) {
                    // General flying melee pursuit for other mobs (zombie, etc.)
                    boolean advanced = data.getRealm().isCoreTier();
                    double accel = advanced ? 0.08 : 0.05;
                    double maxSpd = advanced ? 0.60 : 0.45;
                    int atkCD     = advanced ? 14   : 18;
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

        // Qi: base x5 +1 per stage. Foundation & above use configured multipliers that scale further for later realms.
        double qiScale = ModConfigs.COMMON.creeperQiBaseMult.get()
                + ModConfigs.COMMON.creeperPerStageAddMult.get() * (data.getStage() - 1);

        double realmScale;
        Realm realm = data.getRealm();
        if (realm == Realm.QI_GATHERING) {
            realmScale = 1.0;
        } else if (realm == Realm.FOUNDATION) {
            realmScale = ModConfigs.COMMON.creeperFoundationRealmMult.get();
        } else if (realm.ordinal() >= Realm.CORE_FORMATION.ordinal()) {
            double baseMult = ModConfigs.COMMON.creeperCoreRealmMult.get();
            int extraRealms = Math.max(0, realm.ordinal() - Realm.CORE_FORMATION.ordinal());
            if (extraRealms == 0) {
                realmScale = baseMult;
            } else {
                double step = ModConfigs.COMMON.creeperAdvancedRealmStep.get();
                realmScale = baseMult * Math.pow(step, extraRealms);
            }

            int advancedSteps = Math.max(0, realm.ordinal() - Realm.CORE_FORMATION.ordinal());
            if (advancedSteps > 0) {
                double step = ModConfigs.COMMON.creeperAdvancedRealmStep.get();
                // Apply the configured growth once per realm past Core Formation.
                realmScale *= Math.pow(step, advancedSteps);
            }
        } else {
            realmScale = 1.0;
        }

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

    private static double realmAccel(Realm realm) {
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) return 0.12;
        if (realm.ordinal() >= Realm.CORE_FORMATION.ordinal()) return 0.08;
        return 0.05;
    }

    private static double realmSpeed(Realm realm) {
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) return 0.85;
        if (realm.ordinal() >= Realm.CORE_FORMATION.ordinal()) return 0.60;
        return 0.45;
    }

    private static int realmAttackCooldown(Realm realm) {
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) return 10;
        if (realm.ordinal() >= Realm.CORE_FORMATION.ordinal()) return 14;
        return 18;
    }
}
