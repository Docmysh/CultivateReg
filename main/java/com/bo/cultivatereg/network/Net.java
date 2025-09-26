// src/main/java/com/bo/cultivatereg/network/Net.java
package com.bo.cultivatereg.network;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.MobCultivationData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;


public class Net {
    private static final String PROTO = "6";
    public static SimpleChannel CHANNEL;

    /** Call this once during common setup (you likely already do). */
    public static void init() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CultivateReg.MODID, "main"))
                .networkProtocolVersion(() -> PROTO)
                .clientAcceptedVersions(PROTO::equals)
                .serverAcceptedVersions(PROTO::equals)
                .simpleChannel();

        int id = 0;

        // Core sync
        CHANNEL.registerMessage(id++, SyncCultivationPacket.class,
                SyncCultivationPacket::encode, SyncCultivationPacket::decode, SyncCultivationPacket::handle);

        CHANNEL.registerMessage(id++, SyncMobCultivationPacket.class,
                SyncMobCultivationPacket::encode, SyncMobCultivationPacket::decode, SyncMobCultivationPacket::handle);

        // Meditation / states
        CHANNEL.registerMessage(id++, StartMeditatePacket.class,
                StartMeditatePacket::encode, StartMeditatePacket::decode, StartMeditatePacket::handle);
        CHANNEL.registerMessage(id++, StopMeditatePacket.class,
                StopMeditatePacket::encode, StopMeditatePacket::decode, StopMeditatePacket::handle);

        CHANNEL.registerMessage(id++, StartRestPacket.class,
                StartRestPacket::encode, StartRestPacket::decode, StartRestPacket::handle);
        CHANNEL.registerMessage(id++, StopRestPacket.class,
                StopRestPacket::encode, StopRestPacket::decode, StopRestPacket::handle);

        CHANNEL.registerMessage(id++, StartShieldPacket.class,
                StartShieldPacket::encode, StartShieldPacket::decode, StartShieldPacket::handle);
        CHANNEL.registerMessage(id++, StopShieldPacket.class,
                StopShieldPacket::encode, StopShieldPacket::decode, StopShieldPacket::handle);

        CHANNEL.registerMessage(id++, StartQiSightPacket.class,
                StartQiSightPacket::encode, StartQiSightPacket::decode, StartQiSightPacket::handle);
        CHANNEL.registerMessage(id++, StopQiSightPacket.class,
                StopQiSightPacket::encode, StopQiSightPacket::decode, StopQiSightPacket::handle);

        CHANNEL.registerMessage(id++, StartFlightPacket.class,
                StartFlightPacket::encode, StartFlightPacket::decode, StartFlightPacket::handle);
        CHANNEL.registerMessage(id++, StopFlightPacket.class,
                StopFlightPacket::encode, StopFlightPacket::decode, StopFlightPacket::handle);

        // Early-game sensing -> guide Qi to meridian
        CHANNEL.registerMessage(id++, SenseAttemptPacket.class,
                SenseAttemptPacket::encode, SenseAttemptPacket::decode, SenseAttemptPacket::handle);
        CHANNEL.registerMessage(id++, BreakthroughPacket.class,
                BreakthroughPacket::encode, BreakthroughPacket::decode, BreakthroughPacket::handle);


        // NOTE: Heavenly Sword packets intentionally not registered anymore.
    }

    /** Player cultivation sync (now includes meridian mask + progress). */
    public static void sync(ServerPlayer sp, CultivationData data) {
        int mask = 0;
        byte[] prog = new byte[CultivationData.MERIDIANS];
        for (int i = 0; i < CultivationData.MERIDIANS; i++) {
            if (data.isMeridianOpen(i)) mask |= (1 << i);
            prog[i] = (byte) Math.max(0, Math.min(100, data.getMeridianProgress(i)));
        }

        CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new SyncCultivationPacket(
                        data.getRealm().ordinal(),
                        data.getStage(),
                        data.getQi(),
                        data.isMeditating(),
                        data.hasSensed(),
                        data.getSenseProgress(),
                        data.getSpirit(),
                        data.isResting(),
                        data.isShielding(),
                        data.isFlying(),
                        mask,
                        prog
                )
        );
    }


    public static void syncMobToPlayer(ServerPlayer sp, LivingEntity le, MobCultivationData data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                new SyncMobCultivationPacket(
                        le.getId(),
                        data.hasCultivation(),
                        data.getRealm(),
                        data.getStage()
                )
        );
    }
}
