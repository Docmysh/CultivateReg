package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartRestPacket() {
    public static void encode(StartRestPacket pkt, FriendlyByteBuf buf) {}
    public static StartRestPacket decode(FriendlyByteBuf buf) { return new StartRestPacket(); }

    public static void handle(StartRestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sender = c.getSender();
            if (sender == null) return;

            sender.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData data) -> {
                if (!data.isCultivationUnlocked()) return;
                // Cannot rest while using other abilities
                if (data.isShielding() || data.isFlying()) return;

                // Mutually exclusive with meditation
                data.setMeditating(false);
                data.setResting(true);

                // No more seat entity is needed.

                Net.sync(sender, data);
            });
        });
        c.setPacketHandled(true);
    }
}
