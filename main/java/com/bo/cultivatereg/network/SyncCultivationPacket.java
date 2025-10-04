package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SyncCultivationPacket(
        int realmOrdinal,
        int stage,
        float qi,
        boolean meditating,
        boolean cultivationUnlocked,
        boolean sensed,
        float senseProgress,
        float spirit,
        boolean resting,
        boolean shielding,
        boolean flying,
        ResourceLocation manualId,
        int manualQuizProgress,
        boolean manualQuizPassed,
        int meridianMask,
        byte[] meridianProgress
) {
    public static void encode(SyncCultivationPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.realmOrdinal());
        buf.writeVarInt(pkt.stage());
        buf.writeFloat(pkt.qi());
        buf.writeBoolean(pkt.meditating());
        buf.writeBoolean(pkt.cultivationUnlocked());
        buf.writeBoolean(pkt.sensed());
        buf.writeFloat(pkt.senseProgress());
        buf.writeFloat(pkt.spirit());
        buf.writeBoolean(pkt.resting());
        buf.writeBoolean(pkt.shielding());
        buf.writeBoolean(pkt.flying());
        buf.writeResourceLocation(pkt.manualId());
        buf.writeVarInt(pkt.manualQuizProgress());
        buf.writeBoolean(pkt.manualQuizPassed());
        buf.writeVarInt(pkt.meridianMask());
        buf.writeVarInt(pkt.meridianProgress().length);
        buf.writeByteArray(pkt.meridianProgress());
    }

    public static SyncCultivationPacket decode(FriendlyByteBuf buf) {
        int realmOrdinal = buf.readVarInt();
        int stage = buf.readVarInt();
        float qi = buf.readFloat();
        boolean meditating = buf.readBoolean();
        boolean cultivationUnlocked = buf.readBoolean();
        boolean sensed = buf.readBoolean();
        float senseProgress = buf.readFloat();
        float spirit = buf.readFloat();
        boolean resting = buf.readBoolean();
        boolean shielding = buf.readBoolean();
        boolean flying = buf.readBoolean();
        ResourceLocation manualId = buf.readResourceLocation();
        int manualQuizProgress = buf.readVarInt();
        boolean manualQuizPassed = buf.readBoolean();
        int mask = buf.readVarInt();
        int n = buf.readVarInt();
        byte[] prog = buf.readByteArray(n);
        return new SyncCultivationPacket(
                realmOrdinal, stage, qi,
                meditating, cultivationUnlocked, sensed, senseProgress,
                spirit, resting, shielding, flying,
                manualId, manualQuizProgress, manualQuizPassed,
                mask, prog
        );
    }

    public static void handle(SyncCultivationPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            mc.player.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent(data -> {
                data.setRealm(Realm.values()[pkt.realmOrdinal()]);
                data.setStage(pkt.stage());
                data.setQi(pkt.qi());
                data.setMeditating(pkt.meditating());
                data.setCultivationUnlocked(pkt.cultivationUnlocked());
                data.setSensed(pkt.sensed());
                data.setSenseProgress(pkt.senseProgress());
                data.setSpirit(pkt.spirit());
                data.setResting(pkt.resting());
                data.setShielding(pkt.shielding());
                data.setFlying(pkt.flying());
                data.setManualId(pkt.manualId());
                data.setManualQuizProgress(pkt.manualQuizProgress());
                data.setManualQuizPassed(pkt.manualQuizPassed());

                // meridians
                for (int i = 0; i < CultivationData.MERIDIANS && i < pkt.meridianProgress().length; i++) {
                    data.setMeridianOpen(i, ((pkt.meridianMask() >> i) & 1) != 0);
                    data.setMeridianProgress(i, Byte.toUnsignedInt(pkt.meridianProgress()[i]));
                }
            });
        });
        c.setPacketHandled(true);
    }
}
