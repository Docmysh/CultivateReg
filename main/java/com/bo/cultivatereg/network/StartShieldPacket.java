package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartShieldPacket() {
    public static void encode(StartShieldPacket pkt, FriendlyByteBuf buf) {}
    public static StartShieldPacket decode(FriendlyByteBuf buf) { return new StartShieldPacket(); }

    public static void handle(StartShieldPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                // No realm gate: just need Spirit > 0
                if (data.getSpirit() > 0f) {
                    data.setMeditating(false);
                    data.setResting(false);

                    data.setShielding(true);
                    Net.sync(sp, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
