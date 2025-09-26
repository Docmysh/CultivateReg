// src/main/java/com/bo/cultivatereg/network/StopQiSightPacket.java
package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopQiSightPacket() {
    public static void encode(StopQiSightPacket pkt, FriendlyByteBuf buf) {}
    public static StopQiSightPacket decode(FriendlyByteBuf buf) { return new StopQiSightPacket(); }

    public static void handle(StopQiSightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> d.setQiSight(false));
        });
        c.setPacketHandled(true);
    }
}