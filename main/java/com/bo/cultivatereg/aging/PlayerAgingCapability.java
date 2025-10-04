package com.bo.cultivatereg.aging;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Capability container used to store {@link PlayerAgingData} on players.
 */
@Mod.EventBusSubscriber(modid = CultivateReg.MOD_ID)
public final class PlayerAgingCapability {
    public static final ResourceLocation KEY = new ResourceLocation(CultivateReg.MOD_ID, "aging");
    public static final Capability<PlayerAgingData> PLAYER_AGING_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    private PlayerAgingCapability() {
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player && !player.getCapability(PLAYER_AGING_CAP).isPresent()) {
            event.addCapability(KEY, new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        event.getOriginal().getCapability(PLAYER_AGING_CAP).ifPresent(oldData ->
                event.getEntity().getCapability(PLAYER_AGING_CAP).ifPresent(newData -> newData.copyFrom(oldData)));
    }

    private static class Provider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final PlayerAgingData data = new PlayerAgingData();
        private final LazyOptional<PlayerAgingData> optional = LazyOptional.of(() -> data);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == PLAYER_AGING_CAP ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            data.deserializeNBT(nbt);
        }
    }
}
