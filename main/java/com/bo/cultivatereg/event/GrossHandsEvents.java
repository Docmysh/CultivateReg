package com.bo.cultivatereg.event;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class GrossHandsEvents {
    private static final int POISON_DURATION = 20 * 5;
    private static final int STANK_DURATION = 20 * 300;

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }

        if (!player.hasEffect(ModEffects.GROSS_HANDS.get())) {
            return;
        }

        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity livingEntity)) {
            return;
        }

        livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION));
        livingEntity.addEffect(new MobEffectInstance(ModEffects.STANK.get(), STANK_DURATION));
    }
}