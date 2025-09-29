package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.item.BasicQiManualItem;
import com.bo.cultivatereg.item.BoozeItem;
import com.bo.cultivatereg.item.FilthyCultivationManualItem;
import com.bo.cultivatereg.item.SpiritStoneItem;
import com.bo.cultivatereg.registry.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CultivateReg.MODID);

    public static final RegistryObject<Item> LOW_GRADE_SPIRIT_STONE =
            ITEMS.register("low_spirit_stone", () -> new SpiritStoneItem(0x8E8E8E, Realm.QI_GATHERING, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> SPIRIT_STONE =
            ITEMS.register("spirit_stone", () -> new SpiritStoneItem(0xFFFFFF, Realm.FOUNDATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HIGH_GRADE_SPIRIT_STONE =
            ITEMS.register("high_spirit_stone", () -> new SpiritStoneItem(0xFF9A3C, Realm.CORE_FORMATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> TOP_GRADE_SPIRIT_STONE =
            ITEMS.register("top_spirit_stone", () -> new SpiritStoneItem(0xFF4A4A, Realm.CORE_FORMATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> LOW_SPIRIT_JADE =
            ITEMS.register("low_spirit_jade", () -> new SpiritStoneItem(0x6BD989, Realm.NASCENT_SOUL, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> MID_SPIRIT_JADE =
            ITEMS.register("mid_spirit_jade", () -> new SpiritStoneItem(0x4DB5C8, Realm.SOUL_TRANSFORMATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> HIGH_SPIRIT_JADE =
            ITEMS.register("high_spirit_jade", () -> new SpiritStoneItem(0x3F7BE0, Realm.SPIRIT_SEVERING, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> TOP_SPIRIT_JADE =
            ITEMS.register("top_spirit_jade", () -> new SpiritStoneItem(0x7E4EE1, Realm.VOID_REFINING, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> DIVINE_STONE_SHARD =
            ITEMS.register("divine_stone_shard", () -> new SpiritStoneItem(0xF4D27F, Realm.INTEGRATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> DIVINE_STONE =
            ITEMS.register("divine_stone", () -> new SpiritStoneItem(0xFFF1C1, Realm.TRIBULATION, new Item.Properties().stacksTo(64)));

    public static final RegistryObject<Item> FILTHY_CULTIVATION_MANUAL =
            ITEMS.register("filthy_manual", () -> new FilthyCultivationManualItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BASIC_QI_MANUAL =
            ITEMS.register("basic_qi_manual", () -> new BasicQiManualItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BOOZE =
            ITEMS.register("booze", () -> new BoozeItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> DIRTY_TRASH_CAN_ITEM =
            ITEMS.register("dirty_trash_can", () -> new BlockItem(ModBlocks.DIRTY_TRASH_CAN.get(), new Item.Properties()));
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

}
