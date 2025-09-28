package com.bo.cultivatereg.block;

import com.bo.cultivatereg.entity.HomelessManEntity;
import com.bo.cultivatereg.registry.ModEffects;
import com.bo.cultivatereg.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
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
        if (state.getValue(LOOTED)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        List<HomelessManEntity> entities = level.getEntitiesOfClass(HomelessManEntity.class, new AABB(pos).inflate(5.0D));
        boolean questComplete = entities.stream().anyMatch(HomelessManEntity::isQuestComplete);

        if (!questComplete) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.cultivatereg.trash_can.too_foul"), true);
            return InteractionResult.CONSUME;
        }

        level.setBlock(pos, state.setValue(LOOTED, Boolean.TRUE), Block.UPDATE_CLIENTS);
        Block.popResource(level, pos.above(), ModItems.BASIC_QI_MANUAL.get().getDefaultInstance());
        player.addEffect(new MobEffectInstance(ModEffects.STANK.get(), 20 * 600));
        return InteractionResult.CONSUME;
    }
}