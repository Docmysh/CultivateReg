package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopShieldPacket() {
    public static void encode(StopShieldPacket pkt, FriendlyByteBuf buf) {}
    public static StopShieldPacket decode(FriendlyByteBuf buf) { return new StopShieldPacket(); }

    public static void handle(StopShieldPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                if (data.isShielding()) {
                    data.setShielding(false);
                    Net.sync(sp, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
