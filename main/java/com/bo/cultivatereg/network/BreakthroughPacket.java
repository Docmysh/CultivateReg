package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.bo.cultivatereg.cultivation.manual.CultivationManual;
import com.bo.cultivatereg.cultivation.manual.CultivationManuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BreakthroughPacket {
    public static void encode(BreakthroughPacket pkt, FriendlyByteBuf buf) {}
    public static BreakthroughPacket decode(FriendlyByteBuf buf) { return new BreakthroughPacket(); }

    public static void handle(BreakthroughPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            var sp = c.getSender();
            if (sp == null) return;

            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData d) -> {
                CultivationManual manual = CultivationManuals.byId(d.getManualId());
                if (!d.isManualQuizPassed()) return;
                if (!manual.canBreakthroughFrom(d.getRealm())) return;
                if (manual.targetRealm() != Realm.QI_GATHERING) return;
                if (d.getOpenMeridianCount() < 1) return;   // need at least one opened
                // Optional: require full health or a small Spirit cost, etc.

                d.setRealm(manual.targetRealm());
                d.setStage(1);
                d.setMeditating(false);
                // Give a little starting Qi
                d.setQi(Math.max(d.getQi(), 10f));

                Net.sync(sp, d);
            });
        });
        c.setPacketHandled(true);
    }
}
