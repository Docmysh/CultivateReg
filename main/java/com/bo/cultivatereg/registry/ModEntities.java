package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CultivateReg.MODID);

    // The SEAT entity has been removed as it is no longer needed.

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
    }
}

