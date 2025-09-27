package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.item.SpiritStoneItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CultivateReg.MODID);

    public static final RegistryObject<Item> LOW_GRADE_SPIRIT_STONE =
            ITEMS.register("low_spirit_stone", () -> new SpiritStoneItem(0x8E8E8E, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SPIRIT_STONE =
            ITEMS.register("spirit_stone", () -> new SpiritStoneItem(0xFFFFFF, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HIGH_GRADE_SPIRIT_STONE =
            ITEMS.register("high_spirit_stone", () -> new SpiritStoneItem(0xFF9A3C, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> TOP_GRADE_SPIRIT_STONE =
            ITEMS.register("top_spirit_stone", () -> new SpiritStoneItem(0xFF4A4A, new Item.Properties().stacksTo(64)));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
