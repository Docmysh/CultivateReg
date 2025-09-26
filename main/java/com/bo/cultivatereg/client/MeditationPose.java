// src/main/java/com/bo/cultivatereg/client/MeditationPose.java
package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Forces a lotus meditation pose (even if vanilla 'riding' pose would apply)
 * and renders a pulsing qi orb between the hands.
 * Works on Forge 1.20.1 using only RenderLivingEvent Pre/Post.
 */
@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MeditationPose {

    // ---- Tweak these to move the orb between the hands ----
    private static final float ORB_Y_FACTOR = 0.60f;  // a tad higher
    private static final float ORB_FORWARD  = 0.85f;  // MUCH further forward
    private static final float ORB_SIZE     = 0.46f;  // slightly bigger
    // -------------------------------------------------------

    private static final ResourceLocation QI_TEX =
            new ResourceLocation(CultivateReg.MODID, "textures/effects/qi_ball.png");

    private static final Map<UUID, Saved> SAVED = new HashMap<>();

    private static boolean isMeditating(Player p) {
        return p != null && p.getCapability(CultivationCapability.CULTIVATION_CAP)
                .map(CultivationData::isMeditating).orElse(false);
    }

    private static float rad(float deg) { return (float) Math.toRadians(deg); }

    // Run LAST so our pose wins even if a seat/another mod tweaks angles
    @SubscribeEvent(priority = EventPriority.LOWEST) // run last
    public static void onPre(RenderLivingEvent.Pre<?, ?> e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!(e.getRenderer().getModel() instanceof HumanoidModel<?> m)) return;
        if (!isMeditating(p)) return;

        SAVED.put(p.getUUID(), new Saved(m));

        // Nuke vanilla riding/crouch so they can't force legs/arms
        m.riding = false;
        m.crouching = false;

        // Start from a clean slate so a seat mod can’t keep offsets
        m.rightArm.xRot = m.rightArm.yRot = m.rightArm.zRot = 0f;
        m.leftArm.xRot  = m.leftArm.yRot  = m.leftArm.zRot  = 0f;
        m.rightLeg.xRot = m.rightLeg.yRot = m.rightLeg.zRot = 0f;
        m.leftLeg.xRot  = m.leftLeg.yRot  = m.leftLeg.zRot  = 0f;
        if (m.body != null) { m.body.xRot = m.body.yRot = m.body.zRot = 0f; }
        if (m.head != null) { /* keep head from setupAnim, we’ll only tilt slightly */ }

        // --- Lotus base ---
        if (m.body != null) m.body.xRot = rad(10f);   // slight lean forward
        if (m.head != null) m.head.xRot += rad(-5f);  // gentle head tilt

        // Arms inward, hands meeting over lap/chest
        m.rightArm.xRot = rad(-55f);
        m.leftArm.xRot  = rad(-55f);
        m.rightArm.yRot = rad(-50f);
        m.leftArm.yRot  = rad( 50f);
        m.rightArm.zRot = rad(-15f);
        m.leftArm.zRot  = rad( 15f);

        // Lotus legs (crossed)
        m.rightLeg.xRot = rad(-95f);
        m.leftLeg.xRot  = rad(-95f);
        m.rightLeg.yRot = rad( 60f);
        m.leftLeg.yRot  = rad(-60f);
        m.rightLeg.zRot = rad( 35f);
        m.leftLeg.zRot  = rad(-35f);

        // Breathing micro-motion
        float ft = Minecraft.getInstance().getFrameTime();
        float t = (p.tickCount + ft) / 20.0f;
        float breath = (float) Math.sin(t * Math.PI * 2f) * rad(4.5f);
        float micro  = (float) Math.sin(t * Math.PI * 4f) * rad(1.2f);

        m.rightArm.xRot += breath;        m.leftArm.xRot  += breath;
        m.rightArm.yRot -= breath*0.25f;  m.leftArm.yRot  += breath*0.25f;
        m.rightArm.zRot -= micro;         m.leftArm.zRot  += micro;
        m.rightLeg.xRot += breath*0.25f;  m.leftLeg.xRot  += breath*0.25f;
        if (m.body != null) m.body.xRot += breath*0.15f;
    }


    // Draw the qi orb and restore angles we changed
    @SubscribeEvent
    public static void onPost(RenderLivingEvent.Post<?, ?> e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!(e.getRenderer().getModel() instanceof HumanoidModel<?> m)) return;

        if (isMeditating(p)) {
            renderQiOrb(e.getPoseStack(), e.getMultiBufferSource(), p);
        }

        Saved s = SAVED.remove(p.getUUID());
        if (s != null) s.restore(m);
    }

    // --- Orb rendering ---
    private static void renderQiOrb(PoseStack pose, MultiBufferSource buf, Player p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        float ft = mc.getFrameTime();
        float t = (p.tickCount + ft) / 10.0f;
        float pulse = 0.9f + 0.1f * (float) Math.sin(t * Math.PI * 2f);

        pose.pushPose();

        // Move to chest/lap area, then push slightly forward so it's between hands
        var look = p.getLookAngle();
        double y = p.getBbHeight() * ORB_Y_FACTOR;
        pose.translate(0.0, y, 0.0);
        pose.translate(look.x * ORB_FORWARD, 0.0, look.z * ORB_FORWARD);

        // Face the camera (billboard)
        EntityRenderDispatcher ed = mc.getEntityRenderDispatcher();
        pose.mulPose(ed.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180f));

        float core = ORB_SIZE * pulse;
        float halo = core * 1.6f;
        drawQuad(pose, buf, QI_TEX, halo, 110);
        drawQuad(pose, buf, QI_TEX, core, 210);

        pose.popPose();
    }

    private static void drawQuad(PoseStack pose, MultiBufferSource buf, ResourceLocation tex, float size, int alpha) {
        Matrix4f mat = pose.last().pose();
        Matrix3f nrm = pose.last().normal();
        VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(tex));
        int light = LightTexture.FULL_BRIGHT;

        vc.vertex(mat, -size, -size, 0).color(255,255,255,alpha).uv(0,1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nrm,0,0,1).endVertex();
        vc.vertex(mat,  size, -size, 0).color(255,255,255,alpha).uv(1,1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nrm,0,0,1).endVertex();
        vc.vertex(mat,  size,  size, 0).color(255,255,255,alpha).uv(1,0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nrm,0,0,1).endVertex();
        vc.vertex(mat, -size,  size, 0).color(255,255,255,alpha).uv(0,0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nrm,0,0,1).endVertex();
    }

    // Save/restore so our edits don't leak to other renders
    private static class Saved {
        final float raX, raY, raZ, laX, laY, laZ, rlX, rlY, rlZ, llX, llY, llZ;
        final float bodyX, bodyY, bodyZ, headX;
        final boolean riding, crouching;

        Saved(HumanoidModel<?> m) {
            raX = m.rightArm.xRot; raY = m.rightArm.yRot; raZ = m.rightArm.zRot;
            laX = m.leftArm.xRot;  laY = m.leftArm.yRot;  laZ = m.leftArm.zRot;
            rlX = m.rightLeg.xRot; rlY = m.rightLeg.yRot; rlZ = m.rightLeg.zRot;
            llX = m.leftLeg.xRot;  llY = m.leftLeg.yRot;  llZ = m.leftLeg.zRot;
            bodyX = m.body != null ? m.body.xRot : 0f;
            bodyY = m.body != null ? m.body.yRot : 0f;
            bodyZ = m.body != null ? m.body.zRot : 0f;
            headX = m.head != null ? m.head.xRot : 0f;
            riding = m.riding; crouching = m.crouching;
        }

        void restore(HumanoidModel<?> m) {
            m.rightArm.xRot = raX; m.rightArm.yRot = raY; m.rightArm.zRot = raZ;
            m.leftArm.xRot  = laX; m.leftArm.yRot  = laY; m.leftArm.zRot  = laZ;
            m.rightLeg.xRot = rlX; m.rightLeg.yRot = rlY; m.rightLeg.zRot = rlZ;
            m.leftLeg.xRot  = llX; m.leftLeg.yRot  = llY; m.leftLeg.zRot  = llZ;
            if (m.body != null) { m.body.xRot = bodyX; m.body.yRot = bodyY; m.body.zRot = bodyZ; }
            if (m.head != null) { m.head.xRot = headX; }
            m.riding = riding; m.crouching = crouching;
        }
    }
}
