package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StopFlightPacket() {
    public static void encode(StopFlightPacket pkt, FriendlyByteBuf buf) {}
    public static StopFlightPacket decode(FriendlyByteBuf buf) { return new StopFlightPacket(); }

    public static void handle(StopFlightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                if (data.isFlying()) {
                    data.setFlying(false);
                    if (!sp.isCreative() && !sp.isSpectator()) {
                        sp.getAbilities().flying = false;
                        sp.getAbilities().mayfly = false;
                        sp.onUpdateAbilities();
                    }
                    Net.sync(sp, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
