package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MeditationRenderOffset {
    private static final Set<Integer> PUSHED = new HashSet<>();
    private static final double DOWN_Y_MEDITATE = -0.65; // tweak to taste
    private static final double DOWN_Y_REST     = -0.65; // slightly more reclined/relaxed

    @SubscribeEvent
    public static void onPre(RenderPlayerEvent.Pre e) {
        Player p = e.getEntity();
        if (p != Minecraft.getInstance().player) return; // local player only

        p.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
            double down = 0.0;
            if (d.isMeditating()) down = DOWN_Y_MEDITATE;
            else if (d.isResting()) down = DOWN_Y_REST;

            if (down != 0.0) {
                PoseStack ps = e.getPoseStack();
                ps.pushPose();
                ps.translate(0.0, down, 0.0);
                PUSHED.add(p.getId());
            }
        });
    }

    @SubscribeEvent
    public static void onPost(RenderPlayerEvent.Post e) {
        if (PUSHED.remove(e.getEntity().getId())) {
            e.getPoseStack().popPose();
        }
    }
}
