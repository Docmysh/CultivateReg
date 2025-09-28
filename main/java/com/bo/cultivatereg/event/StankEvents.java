package com.bo.cultivatereg.event;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class StankEvents {
    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModEffects.STANK.get())) {
            return;
        }

        ItemStack stack = event.getItem();
        if (stack.isEdible() && !entity.isInWaterRainOrBubble()) {
            if (entity instanceof Player player && !player.level().isClientSide) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.stank.cannot_eat"), true);
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide && entity.hasEffect(ModEffects.STANK.get()) && entity.isInWaterOrRain()) {
            entity.removeEffect(ModEffects.STANK.get());
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.stank.cleansed"), true);
            }
        }
    }
}