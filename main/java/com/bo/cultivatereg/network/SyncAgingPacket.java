package com.bo.cultivatereg.network;

import com.bo.cultivatereg.aging.PlayerAgingCapability;
import com.bo.cultivatereg.aging.PlayerAgingData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class SyncAgingPacket {
    private final PlayerAgingData data;

    public SyncAgingPacket(PlayerAgingData data) {
        this.data = new PlayerAgingData(data);
    }

    public PlayerAgingData getData() {
        return data;
    }

    public static void encode(SyncAgingPacket msg, FriendlyByteBuf buf) {
        msg.data.writeToBuffer(buf);
    }

    public static SyncAgingPacket decode(FriendlyByteBuf buf) {
        return new SyncAgingPacket(PlayerAgingData.readFromBuffer(buf));
    }

    public static void handle(SyncAgingPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> applyClient(packet)));
        ctx.setPacketHandled(true);
    }

    private static void applyClient(SyncAgingPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        player.getCapability(PlayerAgingCapability.PLAYER_AGING_CAP).ifPresent(cap -> cap.copyFrom(packet.data));
    }
}
