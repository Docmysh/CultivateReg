package com.bo.cultivatereg.item;

import com.bo.cultivatereg.client.gui.ManualStudyScreen;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.manual.CultivationManual;
import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import com.bo.cultivatereg.network.Net;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicQiManualItem extends Item {
    public BasicQiManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CultivationManual manual = CultivationManuals.BASIC_QI_GATHERING;
        if (!level.isClientSide) {
            player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                boolean changed = false;

                if (!manual.id().equals(data.getManualId())) {
                    data.setManualId(manual.id());
                    data.setManualQuizProgress(0);
                    // Do not auto-pass the quiz; the player must complete it from the study screen.
                    data.setManualQuizPassed(false);
                    changed = true;
                }

                if (changed && player instanceof ServerPlayer serverPlayer) {
                    Net.sync(serverPlayer, data);
                }
            });

            player.sendSystemMessage(Component.translatable("item.cultivatereg.basic_qi_manual.message"));
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ManualStudyScreen.open(manual, stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cultivatereg.basic_qi_manual.tooltip").withStyle(ChatFormatting.GRAY));
    }
}