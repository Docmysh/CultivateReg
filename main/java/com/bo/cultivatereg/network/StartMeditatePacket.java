package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartMeditatePacket() {
    public static void encode(StartMeditatePacket pkt, FriendlyByteBuf buf) {}
    public static StartMeditatePacket decode(FriendlyByteBuf buf) { return new StartMeditatePacket(); }

    public static void handle(StartMeditatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sender = c.getSender();
            if (sender == null) return;
            sender.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData data) -> {
                // Cannot meditate while using other abilities
                if (data.isShielding() || data.isFlying()) return;

                if (!data.isMeditating()) {
                    data.setMeditating(true);
                    // The server-side tick handler will now immobilize the player.
                    // No more seat entity is needed.
                    Net.sync(sender, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
