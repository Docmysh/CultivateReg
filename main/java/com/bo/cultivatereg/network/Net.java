package com.bo.cultivatereg.network;

import com.bo.cultivatereg.CultivateReg;
import com.bo.cultivatereg.aging.PlayerAgingData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CultivateReg.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private Net() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncAgingPacket.class, SyncAgingPacket::encode, SyncAgingPacket::decode, SyncAgingPacket::handle);
    }

    public static void syncAging(ServerPlayer player, PlayerAgingData data) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncAgingPacket(data));
    }
}
