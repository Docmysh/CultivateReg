package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class MobCultivationCapability {
    public static final Capability<MobCultivationData> CAP =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "mob_cultivation");

    // package-private impl
    static class MobCultivationDataImpl implements MobCultivationData {
        private boolean has;
        private Realm realm = Realm.MORTAL;
        private int stage = 1;

        @Override public boolean hasCultivation() { return has; }
        @Override public void setHasCultivation(boolean v) { has = v; }
        @Override public Realm getRealm() { return realm; }
        @Override public void setRealm(Realm r) { realm = r; has = (r != Realm.MORTAL); }
        @Override public int getStage() { return stage; }
        @Override public void setStage(int s) { stage = Math.max(1, Math.min(9, s)); }
    }

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final MobCultivationDataImpl backend = new MobCultivationDataImpl();
        private final LazyOptional<MobCultivationData> opt = LazyOptional.of(() -> backend);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean("has", backend.hasCultivation());
            nbt.putInt("realm", backend.getRealm().ordinal());
            nbt.putInt("stage", backend.getStage());
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt == null) return;
            backend.setRealm(Realm.values()[nbt.getInt("realm")]);
            backend.setStage(nbt.getInt("stage"));
            backend.setHasCultivation(nbt.getBoolean("has"));
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> e) {
        Entity obj = e.getObject();
        if (obj instanceof LivingEntity && !(obj instanceof Player)) {
            e.addCapability(KEY, new Provider());
        }
    }
}
