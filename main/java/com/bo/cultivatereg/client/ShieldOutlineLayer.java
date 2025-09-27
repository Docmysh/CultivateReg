package com.bo.cultivatereg.client;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

/**
 * Renders a colored, slightly scaled pass of the player model when Qi Shield is active.
 * Depth testing stays normal, so nothing shows through walls (unlike vanilla glowing).
 */
public class ShieldOutlineLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // scale & alpha are gentle so it reads as an outline, not a solid duplicate
    private static final float SCALE = 1.06f;

    public ShieldOutlineLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose,
                       MultiBufferSource buffers,
                       int packedLight,
                       AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {

        if (player.isInvisible()) return;

        CultivationData data = player.getCapability(CultivationCapability.CULTIVATION_CAP).orElse(null);
        if (data == null || !data.isShielding()) return;

        // Pick Qi color from realm (adjust to your taste or source from capability)
        float[] rgb = qiColorFor(data.getRealm(), data.getStage());

        // Subtle pulse while shielding
        float t = (player.tickCount + partialTicks) * 0.15f;
        float alpha = 0.35f + 0.15f * Mth.sin(t);

        // Render scaled, tinted pass using the player's skin (keeps UVs valid)
        var model = this.getParentModel();
        var tex = player.getSkinTextureLocation();
        var vc = buffers.getBuffer(RenderType.entityTranslucent(tex));

        pose.pushPose();
        pose.scale(SCALE, SCALE, SCALE);
        model.renderToBuffer(pose, vc, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                rgb[0], rgb[1], rgb[2], alpha);
        pose.popPose();
    }

    private static float[] qiColorFor(Realm realm, int stage) {
        // Palette is defined on the realm itself so new realms automatically gain colours.
        return realm.shieldColor();
    }
}
