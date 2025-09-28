package com.bo.cultivatereg.client.render;

import com.bo.cultivatereg.entity.HomelessManEntity;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HomelessManRenderer extends MobRenderer<HomelessManEntity, VillagerModel<HomelessManEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/villager/villager.png");

    public HomelessManRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HomelessManEntity entity) {
        return TEXTURE;
    }
}