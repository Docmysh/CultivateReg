package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Suppress vanilla heart icons so our numeric health display can take over.
 */
@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HealthHudHandler {
    private HealthHudHandler() {}

    @SubscribeEvent
    public static void hideVanillaHearts(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null) {
                return;
            }

            boolean hideHearts = player.getCapability(CultivationCapability.CULTIVATION_CAP)
                    .map(data -> data.isCultivationUnlocked())
                    .orElse(false);

            if (hideHearts) {
                event.setCanceled(true);
            }
        }
    }
}