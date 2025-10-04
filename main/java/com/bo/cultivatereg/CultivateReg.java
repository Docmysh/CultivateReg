package com.bo.cultivatereg;

import com.bo.cultivatereg.network.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
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

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(Net::register);
    }
}
