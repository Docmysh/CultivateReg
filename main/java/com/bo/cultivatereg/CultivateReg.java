package com.bo.cultivatereg;

import com.bo.cultivatereg.config.ModConfigs;
import com.bo.cultivatereg.network.Net;
import com.bo.cultivatereg.registry.ModBlocks;
import com.bo.cultivatereg.registry.ModEffects;
import com.bo.cultivatereg.registry.ModEntities;
import com.bo.cultivatereg.registry.ModItems;
import com.bo.cultivatereg.registry.ModLootModifiers;
import com.bo.cultivatereg.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CultivateReg.MOD_ID)
public final class CultivateReg {
    public static final String MOD_ID = "cultivatereg";
    /** Alternate constant mirroring the naming used elsewhere in the codebase. */
    public static final String MODID = MOD_ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public CultivateReg() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC);

        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModEffects.register(modBus);
        ModEntities.register(modBus);
        ModLootModifiers.register(modBus);
        ModSounds.register(modBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Net::register);
    }
}
