package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.entity.SeatEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CultivateReg.MODID);

    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITIES.register("seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .sized(0.001f, 0.001f)
                    .setTrackingRange(32)
                    .setUpdateInterval(1)
                    .noSummon()
                    .build(ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "seat").toString()));
  ;
}

