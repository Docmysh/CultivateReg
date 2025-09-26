package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRenderLayers {

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (String skin : e.getSkins()) {
            PlayerRenderer r = e.getSkin(skin);
            if (r != null) {
                r.addLayer(new ShieldOutlineLayer(r));
            }
        }
    }
}
