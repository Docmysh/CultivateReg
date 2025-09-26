// src/main/java/com/bo/cultivatereg/network/StartQiSightPacket.java
package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartQiSightPacket() {
    public static void encode(StartQiSightPacket pkt, FriendlyByteBuf buf) {}
    public static StartQiSightPacket decode(FriendlyByteBuf buf) { return new StartQiSightPacket(); }

    public static void handle(StartQiSightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(d -> {
                if (d.hasSensed()) d.setQiSight(true); // gate by having unlocked “Sense”
            });
        });
        c.setPacketHandled(true);
    }
}