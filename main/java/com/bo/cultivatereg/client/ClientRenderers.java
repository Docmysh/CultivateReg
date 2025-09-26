package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.entity.SeatEntity;
import com.bo.cultivatereg.registry.ModEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRenderers {
    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.SEAT.get(), NoopSeatRenderer::new);
    }

    static class NoopSeatRenderer extends EntityRenderer<SeatEntity> {
        public NoopSeatRenderer(EntityRendererProvider.Context ctx) { super(ctx); }
        @Override public void render(SeatEntity e, float yaw, float pt, PoseStack pose, MultiBufferSource buf, int light) {}
        @Override public ResourceLocation getTextureLocation(SeatEntity e) { return null; }
    }
}
