package com.bo.cultivatereg.network;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.aging.PlayerAgingData;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.MobCultivationData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CultivateReg.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private Net() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncCultivationPacket.class, SyncCultivationPacket::encode, SyncCultivationPacket::decode, SyncCultivationPacket::handle);
        CHANNEL.registerMessage(id++, SyncMobCultivationPacket.class, SyncMobCultivationPacket::encode, SyncMobCultivationPacket::decode, SyncMobCultivationPacket::handle);
        CHANNEL.registerMessage(id++, SyncAgingPacket.class, SyncAgingPacket::encode, SyncAgingPacket::decode, SyncAgingPacket::handle);
        CHANNEL.registerMessage(id++, SenseAttemptPacket.class, SenseAttemptPacket::encode, SenseAttemptPacket::decode, SenseAttemptPacket::handle);
        CHANNEL.registerMessage(id++, BreakthroughPacket.class, BreakthroughPacket::encode, BreakthroughPacket::decode, BreakthroughPacket::handle);
        CHANNEL.registerMessage(id++, ManualQuizCompletePacket.class, ManualQuizCompletePacket::encode, ManualQuizCompletePacket::decode, ManualQuizCompletePacket::handle);
        CHANNEL.registerMessage(id++, StartMeditatePacket.class, StartMeditatePacket::encode, StartMeditatePacket::decode, StartMeditatePacket::handle);
        CHANNEL.registerMessage(id++, StopMeditatePacket.class, StopMeditatePacket::encode, StopMeditatePacket::decode, StopMeditatePacket::handle);
        CHANNEL.registerMessage(id++, StartRestPacket.class, StartRestPacket::encode, StartRestPacket::decode, StartRestPacket::handle);
        CHANNEL.registerMessage(id++, StopRestPacket.class, StopRestPacket::encode, StopRestPacket::decode, StopRestPacket::handle);
        CHANNEL.registerMessage(id++, StartShieldPacket.class, StartShieldPacket::encode, StartShieldPacket::decode, StartShieldPacket::handle);
        CHANNEL.registerMessage(id++, StopShieldPacket.class, StopShieldPacket::encode, StopShieldPacket::decode, StopShieldPacket::handle);
        CHANNEL.registerMessage(id++, StartFlightPacket.class, StartFlightPacket::encode, StartFlightPacket::decode, StartFlightPacket::handle);
        CHANNEL.registerMessage(id++, StopFlightPacket.class, StopFlightPacket::encode, StopFlightPacket::decode, StopFlightPacket::handle);
        CHANNEL.registerMessage(id++, StartQiSightPacket.class, StartQiSightPacket::encode, StartQiSightPacket::decode, StartQiSightPacket::handle);
        CHANNEL.registerMessage(id++, StopQiSightPacket.class, StopQiSightPacket::encode, StopQiSightPacket::decode, StopQiSightPacket::handle);
    }

    public static void syncAging(ServerPlayer player, PlayerAgingData data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAgingPacket(data));
    }

    public static void sync(ServerPlayer player, CultivationData data) {
        int mask = 0;
        byte[] progress = new byte[CultivationData.MERIDIANS];
        for (int i = 0; i < CultivationData.MERIDIANS; i++) {
            if (data.isMeridianOpen(i)) {
                mask |= (1 << i);
            }
            progress[i] = (byte) Math.max(0, Math.min(100, data.getMeridianProgress(i)));
        }

        SyncCultivationPacket packet = new SyncCultivationPacket(
                data.getRealm().ordinal(),
                data.getStage(),
                data.getQi(),
                data.isMeditating(),
                data.isCultivationUnlocked(),
                data.hasSensed(),
                data.getSenseProgress(),
                data.getSpirit(),
                data.isResting(),
                data.isShielding(),
                data.isFlying(),
                data.getManualId(),
                data.getManualQuizProgress(),
                data.isManualQuizPassed(),
                mask,
                progress
        );

        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void syncMobToPlayer(ServerPlayer player, LivingEntity entity, MobCultivationData data) {
        SyncMobCultivationPacket packet = new SyncMobCultivationPacket(
                entity.getId(),
                data.hasCultivation(),
                data.getRealm(),
                data.getStage()
        );
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
