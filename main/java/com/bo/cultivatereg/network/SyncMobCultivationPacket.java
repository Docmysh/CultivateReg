// src/main/java/com/bo/cultivatereg/network/SyncMobCultivationPacket.java
package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.MobCultivationCapability;
import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncMobCultivationPacket {
    private final int entityId;
    private final boolean has;
    private final byte realmOrdinal; // ignored when has == false
    private final byte stage;        // 1..9 (clamped)

    public SyncMobCultivationPacket(int entityId, boolean has, Realm realm, int stage) {
        this.entityId = entityId;
        this.has = has;
        this.realmOrdinal = (byte) (realm == null ? 0 : realm.ordinal());
        this.stage = (byte) Math.max(1, Math.min(stage, 9));
    }

    public static void encode(SyncMobCultivationPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.has);
        buf.writeByte(msg.realmOrdinal);
        buf.writeByte(msg.stage);
    }

    public static SyncMobCultivationPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        boolean has = buf.readBoolean();
        byte realmOrd = buf.readByte();
        byte stage = buf.readByte();
        Realm[] realms = Realm.values();
        Realm realm = realms[Math.max(0, Math.min(realmOrd, (byte) (realms.length - 1)))];
        return new SyncMobCultivationPacket(id, has, realm, stage);
    }

    public static void handle(SyncMobCultivationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Entity e = mc.level.getEntity(msg.entityId);
            if (!(e instanceof LivingEntity le)) return;

            le.getCapability(MobCultivationCapability.CAP).ifPresent(data -> {
                data.setHasCultivation(msg.has);
                if (msg.has) {
                    Realm[] realms = Realm.values();
                    int idx = Math.max(0, Math.min(msg.realmOrdinal, (byte) (realms.length - 1)));
                    data.setRealm(realms[idx]);
                    data.setStage(Math.max(1, Math.min(msg.stage, (byte) 9)));
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
