package com.bo.cultivatereg.network;

import com.bo.cultivatereg.cultivation.CultivationCapability;
import com.bo.cultivatereg.cultivation.CultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;          // <-- important
import net.minecraft.world.item.Item;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SenseAttemptPacket(int meridianIndex) {

    public static void encode(SenseAttemptPacket pkt, FriendlyByteBuf buf) { buf.writeVarInt(pkt.meridianIndex); }
    public static SenseAttemptPacket decode(FriendlyByteBuf buf) { return new SenseAttemptPacket(buf.readVarInt()); }

    public static void handle(SenseAttemptPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();            // <-- use ServerPlayer here
            if (sp == null) return;

            sp.getCapability(CultivationCapability.CULTIVATION_CAP).ifPresent((CultivationData d) -> {
                if (!d.isCultivationUnlocked()) return;
                if (!d.isMeditating()) return;

                final int idx = Math.max(0, Math.min(CultivationData.MERIDIANS - 1, pkt.meridianIndex()));
                if (d.isMeridianOpen(idx)) return;

                // === Attempt cost ===
                final var SPIRIT_STONE_ID = ResourceLocation.fromNamespaceAndPath("cultivatereg", "low_spirit_stone");

                if (d.getRealm() == Realm.MORTAL) {
                    // Mortal: must use a Spirit Stone per attempt
                    if (!consumeOne(sp, SPIRIT_STONE_ID)) {
                        return; // no stone → no attempt
                    }
                } else {
                    // Qi Gathering+ : prefer stones; otherwise spend Spirit
                    if (!consumeOne(sp, SPIRIT_STONE_ID)) {
                        final float SPIRIT_COST = 2.0f;
                        if (d.getSpirit() < SPIRIT_COST) return;
                        d.setSpirit(d.getSpirit() - SPIRIT_COST);
                    }
                }

                // === Progress ===
                final int INC = 25; // 4 clicks to open
                int newProg = Math.min(100, d.getMeridianProgress(idx) + INC);
                d.setMeridianProgress(idx, newProg);

                // === Item gates on OPEN (3/6/9) ===
                if (newProg >= 100 && !d.isMeridianOpen(idx)) {
                    int open = d.getOpenMeridianCount(); // open BEFORE this one

                    ResourceLocation need = null; // vanilla placeholders
                    if (open >= 3 && open < 6) {
                        need = ResourceLocation.fromNamespaceAndPath("minecraft", "amethyst_shard");
                    } else if (open >= 6 && open < 9) {
                        need = ResourceLocation.fromNamespaceAndPath("minecraft", "ender_pearl");
                    } else if (open >= 9) {
                        need = ResourceLocation.fromNamespaceAndPath("minecraft", "nether_star");
                    }

                    if (need != null) {
                        if (!consumeOne(sp, need)) {
                            // Keep progress at 100%—bring the item later
                            Net.sync(sp, d);
                            return;
                        }
                    }

                    d.setMeridianOpen(idx, true);
                    if (!d.hasSensed() && d.getOpenMeridianCount() > 0) d.setSensed(true);
                }

                Net.sync(sp, d);                         // <-- sp is ServerPlayer
            });
        });
        c.setPacketHandled(true);
    }

    private static boolean consumeOne(ServerPlayer sp, ResourceLocation id) {
        var reg = sp.level().registryAccess().registryOrThrow(Registries.ITEM);
        Item item = reg.get(id);
        if (item == null) return false;
        var inv = sp.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            var st = inv.getItem(slot);
            if (!st.isEmpty() && st.is(item)) {
                st.shrink(1);
                return true;
            }
        }
        return false;
    }
}
