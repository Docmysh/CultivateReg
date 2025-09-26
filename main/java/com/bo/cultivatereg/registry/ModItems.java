package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CultivateReg.MODID);

    public static final RegistryObject<Item> SPIRIT_STONE =
            ITEMS.register("spirit_stone", () -> new Item(new Item.Properties().stacksTo(64)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
