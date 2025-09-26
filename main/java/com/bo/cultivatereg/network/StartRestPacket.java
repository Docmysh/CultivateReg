package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.registry.ModEntities;
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
                // NEW: cannot rest if shielding or flying
                if (data.isShielding() || data.isFlying()) return;
                // mutually exclusive with meditation
                data.setMeditating(false);
                data.setResting(true);

                // optional: sit the player like meditate does
                var type = ModEntities.SEAT.get(); // reuse your invisible seat
                var level = sender.level();
                if (type != null && !sender.isPassenger()) {
                    var seat = type.create(level);
                    if (seat != null) {
                        seat.moveTo(sender.getX(), Math.floor(sender.getY()), sender.getZ(), sender.getYRot(), sender.getXRot());
                        level.addFreshEntity(seat);
                        sender.startRiding(seat, true);
                    }
                }

                Net.sync(sender, data);
            });
        });
        c.setPacketHandled(true);
    }
}
