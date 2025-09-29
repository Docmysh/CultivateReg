package com.bo.cultivatereg.block;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.entity.HomelessManEntity;
import com.bo.cultivatereg.registry.ModEffects;
import com.bo.cultivatereg.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public class DirtyTrashCanBlock extends Block {
    public static final BooleanProperty LOOTED = BooleanProperty.create("looted");

    public DirtyTrashCanBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LOOTED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LOOTED);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        List<HomelessManEntity> entities = level.getEntitiesOfClass(HomelessManEntity.class, new AABB(pos).inflate(5.0D));
        boolean questComplete = entities.stream().anyMatch(HomelessManEntity::isQuestComplete);

        if (!questComplete) {
            player.sendSystemMessage(Component.translatable("message.cultivatereg.trash_can.too_foul").withStyle(ChatFormatting.GRAY));
            return InteractionResult.CONSUME;
        }

        boolean cultivationUnlocked = player.getCapability(CultivationCapability.CULTIVATION_CAP)
                .map(CultivationData::isCultivationUnlocked)
                .orElse(false);

        if (!cultivationUnlocked) {
            player.sendSystemMessage(Component.translatable("message.cultivatereg.trash_can.need_filthy_manual").withStyle(ChatFormatting.RED));
            return InteractionResult.CONSUME;
        }

        if (!state.getValue(LOOTED)) {
            ItemStack manual = new ItemStack(ModItems.BASIC_QI_MANUAL.get());
            if (!player.addItem(manual)) {
                player.drop(manual, false);
            }

            level.setBlock(pos, state.setValue(LOOTED, Boolean.TRUE), Block.UPDATE_CLIENTS);
            player.sendSystemMessage(Component.translatable("message.cultivatereg.trash_can.find_manual").withStyle(ChatFormatting.GOLD));
        } else {
            player.sendSystemMessage(Component.translatable("message.cultivatereg.trash_can.empty").withStyle(ChatFormatting.GRAY));
        }
        player.addEffect(new MobEffectInstance(ModEffects.STANK.get(), 20 * 600));
        return InteractionResult.CONSUME;
    }
}