package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopRestPacket() {
    public static void encode(StopRestPacket pkt, FriendlyByteBuf buf) {}
    public static StopRestPacket decode(FriendlyByteBuf buf) { return new StopRestPacket(); }

    public static void handle(StopRestPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sender = c.getSender();
            if (sender == null) return;

            sender.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData data) -> {
                data.setResting(false);

                if (sender.getVehicle() instanceof com.bo.cultivatereg.entity.SeatEntity seat) {
                    sender.stopRiding();
                    seat.discard();
                }
                Net.sync(sender, data);
            });
        });
        c.setPacketHandled(true);
    }
}
