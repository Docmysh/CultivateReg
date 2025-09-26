package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record StartFlightPacket() {
    public static void encode(StartFlightPacket pkt, FriendlyByteBuf buf) {}
    public static StartFlightPacket decode(FriendlyByteBuf buf) { return new StartFlightPacket(); }

    public static void handle(StartFlightPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                if (data.getRealm().ordinal() >= Realm.FOUNDATION.ordinal() && data.getSpirit() > 0f) {
                    data.setMeditating(false);
                    data.setResting(false);

                    data.setFlying(true);
                    if (!sp.isCreative() && !sp.isSpectator()) {
                        sp.getAbilities().mayfly = true;
                        sp.getAbilities().flying = true;
                        sp.onUpdateAbilities();
                    }
                    Net.sync(sp, data);
                }
            });
        });
        c.setPacketHandled(true);
    }
}
