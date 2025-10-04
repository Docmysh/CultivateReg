package com.bo.cultivatereg.item;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.network.Net;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class SpiritStoneItem extends Item {
    private static final float DEFAULT_MINUTES_OF_PROGRESS = 15f;
    private static final int ABSORB_DURATION_TICKS = 60;

    private final int color;
    private final Realm requiredRealm;
    private final float minutesOfProgress;

    public SpiritStoneItem(int color, Realm requiredRealm, Properties properties) {
        this(color, requiredRealm, DEFAULT_MINUTES_OF_PROGRESS, properties);
    }

    public SpiritStoneItem(int color, Realm requiredRealm, float minutesOfProgress, Properties properties) {
        super(properties);
        this.color = color;
        this.requiredRealm = requiredRealm;
        this.minutesOfProgress = minutesOfProgress;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && !level.isClientSide) {
            CultivationData data = serverPlayer.getCapability(CultivationCapability.CULTIVATION_CAP)
                    .resolve()
                    .orElse(null);
            if (data == null) {
                return InteractionResultHolder.pass(stack);
            }

            if (!data.isMeditating()) {
                player.displayClientMessage(Component.translatable("message.cultivatereg.spirit_stone.need_meditate"), true);
                return InteractionResultHolder.fail(stack);
            }
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer serverPlayer) {
            handleUse(level, serverPlayer, result);
        }
        return result;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return ABSORB_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    private InteractionResult handleUse(Level level, ServerPlayer player, ItemStack stack) {
        CultivationData data = player.getCapability(CultivationCapability.CULTIVATION_CAP).resolve().orElse(null);
        if (data == null) {
            return InteractionResult.PASS;
        }

        if (!data.isMeditating()) {
            player.displayClientMessage(Component.translatable("message.cultivatereg.spirit_stone.need_meditate"), true);
            return InteractionResult.FAIL;
        }

        Realm playerRealm = data.getRealm();
        if (playerRealm.ordinal() < requiredRealm.ordinal()) {
            overloadPlayer(level, player);
            consumeStone(player, stack);
            return InteractionResult.SUCCESS;
        }

        int stageForStone = requiredRealm == playerRealm ? data.getStage() : 1;
        float qiPerTick = requiredRealm.baseRate * requiredRealm.rateMultiplierForStage(stageForStone)
                * data.getMeridianBonusMultiplier();
        if (qiPerTick <= 0f) {
            return InteractionResult.FAIL;
        }

        float qiGain = qiPerTick * minutesOfProgress * 60f * 20f;
        data.addQi(qiGain);
        consumeStone(player, stack);
        Net.sync(player, data);
        return InteractionResult.SUCCESS;
    }

    private static void consumeStone(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private void explodePlayer(Level level, Player player) {
        level.explode(player, player.getX(), player.getY(), player.getZ(), 4.0f, Level.ExplosionInteraction.NONE);
    }

    private void overloadPlayer(Level level, ServerPlayer player) {
        explodePlayer(level, player);
        player.displayClientMessage(Component.translatable("message.cultivatereg.spirit_stone.overload"), false);

        if (level instanceof ServerLevel serverLevel) {
            player.hurt(serverLevel.damageSources().explosion(player, player), Float.MAX_VALUE);
        } else {
            player.kill();
        }
    }

    public int getColor() {
        return color;
    }

    public Realm getRequiredRealm() {
        return requiredRealm;
    }

    public float getMinutesOfProgress() {
        return minutesOfProgress;
    }
}