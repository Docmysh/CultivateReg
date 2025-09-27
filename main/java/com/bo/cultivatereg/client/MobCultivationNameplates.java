package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID, value = Dist.CLIENT)
public class MobCultivationNameplates {

    // Max squared distance to render the label (32 blocks)
    private static final double MAX_DIST_SQR = 32.0 * 32.0;

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> e) {
        // Gate behind client toggle
        if (!ClientState.isQiSightEnabled()) return;

        LivingEntity entity = e.getEntity();
        Minecraft mc = Minecraft.getInstance();
        Player viewer = mc.player;
        if (viewer == null) return;

        // Only mobs, not players
        if (entity instanceof Player) return;

        // Distance & invisibility cull
        if (viewer.distanceToSqr(entity) > MAX_DIST_SQR) return;
        if (entity.isInvisible()) return;

        entity.getCapability(MobCultivationCapability.CAP).ifPresent(mobData -> {
            if (!mobData.hasCultivation()) return;

            // Obscure if >= 2 realms above viewer
            boolean obscure = isTwoRealmsAbove(viewer, mobData.getRealm());

            String text = obscure
                    ? "??"
                    : shortRealm(mobData.getRealm()) + " " + Mth.clamp(mobData.getStage(), 1, 9) + "/9";

            int color = realmColor(mobData.getRealm());
            drawWorldLabel(e.getPoseStack(), e.getMultiBufferSource(), e.getPackedLight(), entity, text, color, 0.6f);
        });
    }

    // "??" if target is >= 2 realms above the viewer
    private static boolean isTwoRealmsAbove(Player viewer, Realm targetRealm) {
        var cap = viewer.getCapability(CultivationCapability.CULTIVATION_CAP).resolve().orElse(null);
        if (cap == null) return false; // if unknown, show info
        int gap = targetRealm.ordinal() - cap.getRealm().ordinal();
        return gap >= 2;
    }

    // Draw a small, always-facing label above the entity's head
    private static void drawWorldLabel(PoseStack pose, MultiBufferSource buffers, int packedLight,
                                       LivingEntity entity, String text, int color, float yOffset) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        pose.pushPose();
        // Position above head
        double y = entity.getBbHeight() + yOffset;
        pose.translate(0.0D, y, 0.0D);
        // Face the camera
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        // Scale down
        float scale = 0.025F;
        pose.scale(-scale, -scale, scale);

        // Background (like vanilla nametags)
        int half = font.width(text) / 2;
        float bgAlpha = mc.options.getBackgroundOpacity(0.25F);
        int bgColor = (int)(bgAlpha * 255.0F) << 24;

        var mtx = pose.last().pose();
        font.drawInBatch(text, -half, 0, color, false, mtx, buffers, Font.DisplayMode.NORMAL, bgColor, packedLight);

        pose.popPose();
    }

    // Helpers: display + colors
    private static String shortRealm(Realm r) {
        return r.shortName();
    }

    private static int realmColor(Realm r) {
        // ARGB
        return r.nameplateColor();
        };
    }

