package com.bo.cultivatereg.cultivation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class MobCultivationAttributes {
    private static final UUID MAX_HEALTH_MOD     = UUID.fromString("89e9c8e0-2b6a-4c69-9d7c-8ff77c1f6b01");
    private static final UUID ATTACK_DAMAGE_MOD  = UUID.fromString("10de6b1b-7a38-4b55-b8d1-04f2c3a73302");
    private static final UUID MOVE_SPEED_MOD     = UUID.fromString("f3b3f7f2-5c44-4ae0-8dd7-3c1a6a2cb303");
    private static final UUID ARMOR_MOD          = UUID.fromString("0a9f64f7-5a2a-4a1e-9c0e-0e0c1cbe4404");

    private MobCultivationAttributes() {}

    public static void apply(LivingEntity le, MobCultivationData data) {
        double mult = data.getRealm().mobPowerMultiplier(data.getStage()); // health & damage
        applyMul(le, Attributes.MAX_HEALTH,     MAX_HEALTH_MOD,    mult);
        applyMul(le, Attributes.ATTACK_DAMAGE,  ATTACK_DAMAGE_MOD, mult);

        double spdMult = 1.0 + (mult - 1.0) * 0.30; // speed increases less than raw power
        applyMul(le, Attributes.MOVEMENT_SPEED, MOVE_SPEED_MOD, spdMult);

        double extraArmor = data.getRealm().mobArmorBonus(data.getStage());
        applyAdd(le, Attributes.ARMOR, ARMOR_MOD, extraArmor);

        AttributeInstance hp = le.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) le.setHealth((float) hp.getValue());
    }

    private static void applyMul(LivingEntity le, net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, double multiplier) {
        AttributeInstance inst = le.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        double amount = multiplier - 1.0; // MULTIPLY_TOTAL uses (1 + amount)
        if (Math.abs(amount) < 1e-6) return;
        inst.addPermanentModifier(new AttributeModifier(id, "CR Cultivation Mul", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void applyAdd(LivingEntity le, net.minecraft.world.entity.ai.attributes.Attribute attr, UUID id, double amount) {
        AttributeInstance inst = le.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (Math.abs(amount) < 1e-6) return;
        inst.addPermanentModifier(new AttributeModifier(id, "CR Cultivation Add", amount, AttributeModifier.Operation.ADDITION));
    }
}
