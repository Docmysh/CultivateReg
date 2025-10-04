package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.manual.CultivationManual;
import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent by the client after successfully completing a manual's comprehension quiz.
 */
public record ManualQuizCompletePacket(ResourceLocation manualId) {
    public static void encode(ManualQuizCompletePacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(pkt.manualId());
    }

    public static ManualQuizCompletePacket decode(FriendlyByteBuf buf) {
        return new ManualQuizCompletePacket(buf.readResourceLocation());
    }

    public static void handle(ManualQuizCompletePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sender = c.getSender();
            if (sender == null) {
                return;
            }

            CultivationManual manual = CultivationManuals.byId(pkt.manualId());

            sender.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                boolean changed = false;

                if (!manual.id().equals(data.getManualId())) {
                    data.setManualId(manual.id());
                    changed = true;
                }

                int quizSize = manual.quiz().size();
                if (data.getManualQuizProgress() < quizSize) {
                    data.setManualQuizProgress(quizSize);
                    changed = true;
                }

                if (!data.isManualQuizPassed()) {
                    data.setManualQuizPassed(true);
                    changed = true;
                }

                if (changed) {
                    Net.sync(sender, data);
                }
            });

            sender.sendSystemMessage(Component.translatable("message.cultivatereg.manual.completed", manual.displayName()));
        });
        c.setPacketHandled(true);
    }
}