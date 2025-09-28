package com.bo.cultivatereg.registry;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.block.DirtyTrashCanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CultivateReg.MODID);

    public static final RegistryObject<Block> DIRTY_TRASH_CAN = BLOCKS.register("dirty_trash_can",
            () -> new DirtyTrashCanBlock(BlockBehaviour.Properties.copy(Blocks.BARREL)
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}