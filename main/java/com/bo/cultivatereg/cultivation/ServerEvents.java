package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.aging.AgingProfile;
import com.bo.cultivatereg.aging.AgingTuning;
import com.bo.cultivatereg.aging.PlayerAgingCapability;
import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import com.bo.cultivatereg.network.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class ServerEvents {

    // ---- Tuning knobs ----
    private static final float SHIELD_REDUCTION = 0.40f;     // 40% damage reduction
    private static final float SHIELD_COST_PER_DAMAGE = 5f;  // spirit per 1.0 damage reduced
    private static final float SHIELD_BASE_DRAIN_PER_SEC = 4f;

    private static final float FLIGHT_DRAIN_PER_SEC = 10f;   // base spirit per second
    private static final float FLIGHT_SPRINT_BONUS_PER_SEC = 5f;

    // --- Sync on session starts ---
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                Net.sync(sp, d);
                // Apply attributes right now (don’t wait for the tick loop)
                CultivationAttributes.apply(sp, d);
                // If we have extra hearts, fill them so they’re not empty on join
                float max = sp.getMaxHealth();
                if (max > 20.0f && sp.getHealth() < max) {
                    sp.setHealth(max);
                }
            });
            sp.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(age -> Net.syncAging(sp, age));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                Net.sync(sp, d);
                CultivationAttributes.apply(sp, d);
                // Vanilla usually restores to max on respawn, but ensure it:
                sp.setHealth(sp.getMaxHealth());
            });
            sp.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(age -> Net.syncAging(sp, age));
        }
    }

    @SubscribeEvent
    public static void onDimChange(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) {
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                Net.sync(sp, d);
                // Optional: re-apply so max health reflects realm immediately after TP
                CultivationAttributes.apply(sp, d);
            });
            sp.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(age -> Net.syncAging(sp, age));
        }
    }


    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.level().isClientSide) return;
        Player p = e.player;

        p.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
            Realm prevRealm = data.getRealm();
            boolean dirty = false, bigChange = false;
            boolean oncePerSecond = (p.level().getGameTime() % 20L) == 0L;

            // --- NEW: Immobilize player when meditating or resting ---
            if (data.isMeditating() || data.isResting()) {
                p.setDeltaMovement(0, p.getDeltaMovement().y, 0); // Stop horizontal movement
                p.hurtMarked = true; // Helps prevent knockback
            }

            // --- Realm gating: ONLY flight is gated to Foundation+ ---
            if (data.getRealm().ordinal() < Realm.FOUNDATION.ordinal()) {
                if (data.isFlying()) {
                    data.setFlying(false);
                    if (p instanceof ServerPlayer sp && !sp.isCreative() && !sp.isSpectator()) {
                        sp.getAbilities().flying = false;
                        sp.getAbilities().mayfly = false;
                        sp.onUpdateAbilities();
                    }
                    dirty = true;
                }
                // Shield is NOT gated; do nothing here.
            }

            // --- RESTING: spirit/health regen, no breakthrough Qi ---
            if (data.isResting() && oncePerSecond) {
                int cap = spiritCap(data.getRealm(), data.getStage());
                if (cap > 0) {
                    float add = cap * spiritRegenPerSecond(data.getRealm());
                    data.addSpirit(add);
                    if (data.getSpirit() > cap) data.setSpirit(cap);
                    dirty = true;
                }
                float hAdd = healthRegenPerSecond(data.getRealm());
                if (hAdd > 0f && p.getHealth() < p.getMaxHealth()) p.heal(hAdd);
            }

            // --- MEDITATING (breakthrough Qi only) ---
            if (data.isMeditating()) {
                if (data.hasSensed() && data.getRealm() != Realm.MORTAL) {
                    float rate = data.getRealm().baseRate * data.getRealm().rateMultiplierForStage(data.getStage())
                            * data.getMeridianBonusMultiplier();
                    data.addQi(rate);
                    dirty = true;
                    boolean progressed = false;
                    var manual = CultivationManuals.byId(data.getManualId());
                    while (data.getRealm() != Realm.MORTAL) {
                        var stageCap = manual.stageCapFor(data.getRealm());
                        if (stageCap.isPresent() && data.getStage() >= stageCap.getAsInt()) {
                            int limit = data.getRealm().capForStage(data.getStage());
                            if (limit > 0 && data.getQi() > limit) {
                                data.setQi(limit);
                            }
                            break;
                        }
                        int cap = data.getRealm().capForStage(data.getStage());
                        if (data.getQi() + 1e-6f >= cap) {
                            if (data.getStage() < 9) {
                                data.setStage(data.getStage() + 1);
                                data.setQi(data.getQi() - cap);
                            } else {
                                Realm next = data.getRealm().nextMajor();
                                if (next == data.getRealm()) {
                                    data.setQi(Math.min(data.getQi(), cap));
                                    break;
                                }
                                data.setRealm(next);
                                data.setStage(1);
                                data.setQi(data.getQi() - cap);
                            }
                            progressed = true;
                        } else break;
                    }
                    bigChange |= progressed;
                }
            } else {
                if (!data.hasSensed() && data.getSenseProgress() > 0f) {
                    data.setSenseProgress(Math.max(0f, data.getSenseProgress() - 0.5f));
                    dirty = true;
                }
            }

            // --- Shield upkeep drain (NO realm check) ---
            if (data.isShielding() && oncePerSecond) {
                float cost = SHIELD_BASE_DRAIN_PER_SEC;
                if (data.getSpirit() < cost) {
                    data.setShielding(false);
                    dirty = true;
                } else {
                    data.addSpirit(-cost);
                    dirty = true;
                }
            }

            // --- Flight upkeep drain + ability enforcement (Foundation gated) ---
            if (data.isFlying() && data.getRealm().ordinal() >= Realm.FOUNDATION.ordinal()) {
                if (p instanceof ServerPlayer sp && !sp.isCreative() && !sp.isSpectator()) {
                    if (!sp.getAbilities().mayfly) {
                        sp.getAbilities().mayfly = true;
                        sp.getAbilities().flying = true;
                        sp.onUpdateAbilities();
                    }
                }
                if (oncePerSecond) {
                    float cost = FLIGHT_DRAIN_PER_SEC + (p.isSprinting() ? FLIGHT_SPRINT_BONUS_PER_SEC : 0f);
                    if (data.getSpirit() < cost) {
                        data.setFlying(false);
                        if (p instanceof ServerPlayer sp && !sp.isCreative() && !sp.isSpectator()) {
                            sp.getAbilities().flying = false;
                            sp.getAbilities().mayfly = false;
                            sp.onUpdateAbilities();
                        }
                        dirty = true;
                    } else {
                        data.addSpirit(-cost);
                        dirty = true;
                    }
                }
            }

            if (p instanceof ServerPlayer sp) {
                if (bigChange || (dirty && oncePerSecond)) Net.sync(sp, data);
                if (data.getRealm() != prevRealm) {
                    applyRealmAgingSync(sp, data.getRealm());
                }
            }
        });
    }

    private static void applyRealmAgingSync(ServerPlayer sp, Realm realm) {
        sp.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(age -> {
            AgingProfile profile = AgingTuning.profileForRealm(realm);
            age.setRealm(realm);
            age.setMaxLifespanDays(profile.maxLifespanDays());
            age.setAgingMultiplier(profile.agingMultiplier());
            age.setGraceDays(AgingTuning.graceDays());
            Net.syncAging(sp, age);
        });
    }

    // Damage hook: apply shield reduction & Spirit cost; cancel rest/med
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
            boolean changed = false;

            // cancel restful states
            if (data.isMeditating()) { data.setMeditating(false); changed = true; }
            if (data.isResting())    { data.setResting(false);    changed = true; }

            // Shield is NOT realm-gated
            // apply shield if active
            if (data.isShielding()) {
                float dmg = e.getAmount();
                if (dmg > 0f) {
                    // --- figure out attacker's cultivation ---
                    AttackerInfo ai = AttackerInfo.fromDamageSource(e);

                    // --- compute effective reduction ---
                    float reduction = computeShieldReduction(
                            data.getRealm(), data.getStage(),
                            ai.realm, ai.stage
                    ); // 0..1

                    float desiredReduce = dmg * reduction;
                    float costNeeded = desiredReduce * SHIELD_COST_PER_DAMAGE;

                    if (data.getSpirit() >= costNeeded) {
                        e.setAmount(dmg - desiredReduce);
                        data.addSpirit(-costNeeded);
                        changed = true;
                    } else if (data.getSpirit() > 0f) {
                        float ratio = data.getSpirit() / costNeeded;
                        float reduce = desiredReduce * ratio;
                        e.setAmount(dmg - reduce);
                        data.setSpirit(0f);
                        data.setShielding(false); // out of juice
                        changed = true;
                    } else {
                        data.setShielding(false);
                        changed = true;
                    }
                }
            }


            if (changed) Net.sync(sp, data);
        });
    }

    // ---------- helpers ----------
    private static int spiritCap(Realm realm, int stage) {
        return realm.spiritCapForStage(stage);
    }
    private static float spiritRegenPerSecond(Realm realm) {
        return realm.spiritRegenPerSecond();
    }
    private static float healthRegenPerSecond(Realm realm) {
        return realm.healthRegenPerSecond();
    }
    // --- Shield scaling math ---
    private static float computeShieldReduction(Realm defRealm, int defStage, Realm atkRealm, int atkStage) {
        if (defStage < 1) defStage = 1;
        if (defStage > 9) defStage = 9;
        if (atkStage < 1) atkStage = 1;
        if (atkStage > 9) atkStage = 9;

        float base = 0.40f; // 40% vs equal realm
        int realmDelta = defRealm.ordinal() - atkRealm.ordinal();

        float realmBonus = 0f;
        if (realmDelta > 0) {
            realmBonus = 0.40f * realmDelta; // +40% per realm you are above
        }

        float stageBonus = 0f;
        if (defRealm == atkRealm) {
            int stageDelta = defStage - atkStage;
            if (stageDelta > 0) stageBonus = 0.02f * stageDelta; // +2% per stage above
        }
        // If you ALSO want stage bonus across realms, replace the if above with:
        // int stageDelta = defStage - atkStage; if (stageDelta > 0) stageBonus = 0.02f * stageDelta;

        float total = base + realmBonus + stageBonus;
        if (total > 1f) total = 1f;
        if (total < 0f) total = 0f;
        return total;
    }

    // --- Resolve attacker realm/stage from event ---
    private record AttackerInfo(Realm realm, int stage) {
        static AttackerInfo of(Realm r, int s) { return new AttackerInfo(r, s); }
        static AttackerInfo unknown() { return new AttackerInfo(Realm.MORTAL, 1); }

        static AttackerInfo fromDamageSource(net.minecraftforge.event.entity.living.LivingHurtEvent e) {
            var src = e.getSource();
            var entity = src.getEntity();        // the entity responsible (player/mob/projectile)
            // Try owner if projectile
            if (entity instanceof net.minecraft.world.entity.projectile.Projectile proj && proj.getOwner() instanceof net.minecraft.world.entity.LivingEntity le) {
                entity = le;
            }

            if (entity instanceof net.minecraft.world.entity.player.Player p) {
                var cap = p.getCapability(CultivationCapability.CULTIVATION_CAP).resolve().orElse(null);
                if (cap != null) return AttackerInfo.of(cap.getRealm(), cap.getStage());
            } else if (entity instanceof net.minecraft.world.entity.LivingEntity le) {
                var cap = le.getCapability(MobCultivationCapability.CAP).resolve().orElse(null);
                if (cap != null && cap.hasCultivation()) return AttackerInfo.of(cap.getRealm(), cap.getStage());
            }
            // Unknown/environmental: treat as equal realm baseline
            return AttackerInfo.of(Realm.MORTAL, 1);
        }
    }

}
