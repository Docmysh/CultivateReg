package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CultivateReg.MODID);

    public static final RegistryObject<SoundEvent> HEAVENLY_SWORD_WARBLE =
            SOUND_EVENTS.register("heavenly_sword_warble",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "heavenly_sword_warble")));

    public static final RegistryObject<SoundEvent> HEAVENLY_SWORD_SLICE =
            SOUND_EVENTS.register("heavenly_sword_slice",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "heavenly_sword_slice")));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
