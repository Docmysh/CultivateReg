package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopMeditatePacket() {

    public static void encode(StopMeditatePacket pkt, FriendlyByteBuf buf) {
        // no payload
    }

    public static StopMeditatePacket decode(FriendlyByteBuf buf) {
        return new StopMeditatePacket();
    }

    public static void handle(StopMeditatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var context = ctx.get();
        context.enqueueWork(() -> {
            var sender = context.getSender(); // null on client; non-null on server
            if (sender == null) return;

            sender.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData data) -> {
                if (!data.isMeditating()) return;

                // stop meditating
                data.setMeditating(false);

                // if we're sitting on our invisible seat entity, dismount and remove it
                if (sender.getVehicle() instanceof com.bo.cultivatereg.entity.SeatEntity seat) {
                    sender.stopRiding();
                    seat.discard(); // server side: remove the seat entity
                }

                // sync new state to the client
                Net.sync(sender, data);
            });
        });
        context.setPacketHandled(true);
    }
}
