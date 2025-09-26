package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.loot.AddSpiritStoneModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, CultivateReg.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_SPIRIT_STONE =
            LOOT.register("add_spirit_stone", () -> AddSpiritStoneModifier.CODEC);

    public static void register(IEventBus modBus) {
        LOOT.register(modBus);
    }
}
