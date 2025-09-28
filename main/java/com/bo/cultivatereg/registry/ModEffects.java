package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.effect.StankEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CultivateReg.MODID);

    public static final RegistryObject<MobEffect> STANK = EFFECTS.register("stank", StankEffect::new);

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}