package com.bo.cultivatereg.item;

import com.bo.cultivatereg.client.gui.ManualStudyScreen;
import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
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

public class FilthyCultivationManualItem extends Item {
    public FilthyCultivationManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CultivationManual manual = CultivationManuals.FILTHY_BEGGAR_PRIMER;

        if (!level.isClientSide) {
            CultivationData data = player.getCapability(CultivationCapability.CULTIVATION_CAP).orElse(null);
            boolean unlockedBefore = data != null && data.isCultivationUnlocked();

            if (data != null && !unlockedBefore) {
                data.setCultivationUnlocked(true);
                data.setManualId(manual.id());
                data.setManualQuizProgress(manual.quiz().size());
                data.setManualQuizPassed(true);

                if (player instanceof ServerPlayer serverPlayer) {
                    Net.sync(serverPlayer, data);
                }
            }

            String key = unlockedBefore
                    ? "item.cultivatereg.filthy_manual.message.repeat"
                    : "item.cultivatereg.filthy_manual.message.unlock";
            player.sendSystemMessage(Component.translatable(key));
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ManualStudyScreen.open(manual, stack));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.cultivatereg.filthy_manual.tooltip").withStyle(ChatFormatting.GRAY));
    }
}