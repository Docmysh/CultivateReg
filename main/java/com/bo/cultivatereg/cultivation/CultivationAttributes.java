package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CultivationAttributes {
    private CultivationAttributes() {}

    // Stable UUIDs for each attribute modifier
    private static final UUID MAX_HEALTH_MOD    = UUID.fromString("2f6d4d1a-6e6f-4c9b-8f1c-0f7a4c1e0101");
    private static final UUID MOVE_SPEED_MOD    = UUID.fromString("e8b1a2e3-2b7e-4d0f-8a9b-2f8f2a3b4c5d");
    private static final UUID ATTACK_DAMAGE_MOD = UUID.fromString("7c9f0e12-1234-4c7a-9a2d-77d4cba0cafe");
    private static final UUID KNOCKBACK_RES_MOD = UUID.fromString("a4de3f30-b75d-43f1-86fd-7a1b8f6a0a77");

    // Re-apply at END tick so changes survive deaths, swaps, etc.
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Player p = e.player;
        p.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> apply(p, data));
    }

    public static void apply(Player p, CultivationData data) {
        Mods m = computeMods(data);

        // Health (ADDITION): values are raw HP (1 heart = 2.0)
        applyAdd(p, Attributes.MAX_HEALTH, MAX_HEALTH_MOD, m.hpAdd());

        // Attack damage (ADDITION)
        applyAdd(p, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MOD, m.dmgAdd());

        // Movement speed (MULTIPLY_TOTAL): amount is +% (0.10 = +10%)
        applyMul(p, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MOD, m.speedMul());

        // Knockback resistance (MULTIPLY_TOTAL)
        applyMul(p, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_RES_MOD, m.kbMul());

        // Clamp current health to new max
        AttributeInstance hp = p.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && p.getHealth() > hp.getValue()) {
            p.setHealth((float) hp.getValue());
        }
    }

    private static void applyAdd(Player p, net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, double amount) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (Math.abs(amount) < 1e-6) return;
        inst.addTransientModifier(new AttributeModifier(id, "CR Player Cultivation Add", amount, AttributeModifier.Operation.ADDITION));
    }

    private static void applyMul(Player p, net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, double amount) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (Math.abs(amount) < 1e-6) return;
        inst.addTransientModifier(new AttributeModifier(id, "CR Player Cultivation Mul", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private record Mods(double hpAdd, double speedMul, double dmgAdd, double kbMul) {}

    // Tuning that matched what you had working
    private static Mods computeMods(CultivationData data) {
        Realm realm = data.getRealm();
        int stage = Math.max(1, Math.min(9, data.getStage()));
        double mult = Math.max(0.0, data.getMeridianBonusMultiplier());
        switch (realm) {
            case MORTAL:
                return new Mods(0.0, 0.0, 0.0, 0.0);
            case QI_GATHERING:
                // +1 heart per stage, +2% speed/KB per stage, +0.5 dmg per stage
                return new Mods(2.0 * stage * mult, 0.02 * stage * mult, 0.5 * stage * mult, 0.02 * stage * mult);
            case FOUNDATION:
                // flat boost + per-stage growth
                return new Mods((20 + 2.0 * stage) * mult, (0.05 + 0.03 * stage) * mult,
                        (2.0 + 0.75 * stage) * mult, (0.05 + 0.03 * stage) * mult);
            case CORE_FORMATION:
                return new Mods((40 + 4.0 * stage) * mult, (0.10 + 0.05 * stage) * mult,
                        (5.0 + 1.0 * stage) * mult, (0.10 + 0.05 * stage) * mult);
            case NASCENT_SOUL:
                return new Mods(80 + 6.0 * stage, 0.15 + 0.06 * stage, 10.0 + 1.25 * stage, 0.15 + 0.06 * stage);
            case SOUL_TRANSFORMATION:
                return new Mods(120 + 8.0 * stage, 0.20 + 0.07 * stage, 15.0 + 1.50 * stage, 0.20 + 0.07 * stage);
            case SPIRIT_SEVERING:
                return new Mods(160 + 10.0 * stage, 0.25 + 0.08 * stage, 20.0 + 1.75 * stage, 0.25 + 0.08 * stage);
            case VOID_REFINING:
                return new Mods(200 + 12.0 * stage, 0.30 + 0.09 * stage, 25.0 + 2.00 * stage, 0.30 + 0.09 * stage);
            case INTEGRATION:
                return new Mods(240 + 14.0 * stage, 0.35 + 0.10 * stage, 30.0 + 2.25 * stage, 0.35 + 0.10 * stage);
            case TRIBULATION:
                return new Mods(280 + 16.0 * stage, 0.40 + 0.12 * stage, 35.0 + 2.50 * stage, 0.40 + 0.12 * stage);
        }
        return new Mods(0, 0, 0, 0);
    }
}
