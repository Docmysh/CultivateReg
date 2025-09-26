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
                if (data.isShielding() || data.isFlying()) return;
                if (!data.isMeditating()) {
                    data.setMeditating(true);
                    var level = sender.level();
                    if (!level.isClientSide) {
                        var type = com.bo.cultivatereg.registry.ModEntities.SEAT.get();
                        var seat = type.create(level);
                        if (seat != null) {
                            seat.moveTo(sender.getX(), Math.floor(sender.getY()), sender.getZ(), sender.getYRot(), sender.getXRot());
                            level.addFreshEntity(seat);
                            sender.startRiding(seat, true);
                        }
                    }
                    Net.sync(sender, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
