// src/main/java/com/bo/cultivatereg/client/ClientOverlays.java
package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientOverlays {
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("cultivation_hud", HudOverlay.renderOverlay);
    }
}
