package com.bo.cultivatereg;

import com.bo.cultivatereg.config.ModConfigs;
import com.bo.cultivatereg.network.Net;
import com.bo.cultivatereg.registry.ModEntities;
import com.bo.cultivatereg.registry.ModSounds;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CultivateReg.MODID)
public class CultivateReg {
    public static final String MODID = "cultivatereg";

    public CultivateReg() {
        // config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.SPEC);

        // network
        Net.init();

        // mod event bus
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSounds.SOUND_EVENTS.register(modBus);
        com.bo.cultivatereg.registry.ModItems.register(modBus);
        com.bo.cultivatereg.registry.ModLootModifiers.register(modBus);
        ModEntities.register(modBus); // Updated call
    }
}
