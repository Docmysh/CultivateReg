// src/main/java/com/bo/cultivatereg/client/ClientSetup.java
package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.client.render.HomelessManRenderer;
import com.bo.cultivatereg.item.SpiritStoneItem;
import com.bo.cultivatereg.registry.ModEntities;
import com.bo.cultivatereg.registry.ModItems;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.HOMELESS_MAN.get(), HomelessManRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item e) {
        e.register((stack, tintIndex) -> {
                    if (tintIndex != 0) return 0xFFFFFF;
                    if (stack.getItem() instanceof SpiritStoneItem stone) {
                        return stone.getColor();
                    }
                    return 0xFFFFFF;
                },
                ModItems.LOW_GRADE_SPIRIT_STONE.get(),
                ModItems.SPIRIT_STONE.get(),
                ModItems.HIGH_GRADE_SPIRIT_STONE.get(),
                ModItems.TOP_GRADE_SPIRIT_STONE.get()
        );
    }
}