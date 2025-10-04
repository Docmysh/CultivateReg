package com.bo.cultivatereg.client;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.config.ModConfigs;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class CustomAssetTextures {
    private static final ResourceLocation DEFAULT_BOOZE =
            ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "textures/item/booze.png");
    private static final ResourceLocation DEFAULT_DIRTY_TRASHCAN =
            ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "textures/block/dirty_trashcan.png");
    private static final ResourceLocation DEFAULT_HOMELESS =
            ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "textures/entity/homeless.png");

    private static ResourceLocation boozeTexture = DEFAULT_BOOZE;
    private static ResourceLocation dirtyTrashcanTexture = DEFAULT_DIRTY_TRASHCAN;
    private static ResourceLocation homelessTexture = DEFAULT_HOMELESS;

    private CustomAssetTextures() {
    }

    public static void refreshFromConfig() {
        boozeTexture = parse(ModConfigs.COMMON.boozeTexture.get(), DEFAULT_BOOZE, "boozeTexture");
        dirtyTrashcanTexture = parse(ModConfigs.COMMON.dirtyTrashcanTexture.get(), DEFAULT_DIRTY_TRASHCAN,
                "dirtyTrashcanTexture");
        homelessTexture = parse(ModConfigs.COMMON.homelessSkinTexture.get(), DEFAULT_HOMELESS, "homelessSkinTexture");
    }

    private static ResourceLocation parse(String value, ResourceLocation fallback, String key) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        if (parsed == null) {
            CultivateReg.LOGGER.warn("Invalid resource location '{}' for {}; falling back to {}", value, key, fallback);
            return fallback;
        }
        return parsed;
    }

    public static ResourceLocation getBoozeTexture() {
        return boozeTexture;
    }

    public static ResourceLocation getDirtyTrashcanTexture() {
        return dirtyTrashcanTexture;
    }

    public static ResourceLocation getHomelessTexture() {
        return homelessTexture;
    }
}