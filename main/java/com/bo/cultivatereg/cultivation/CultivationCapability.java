package com.bo.cultivatereg.cultivation;

import com.bo.cultivatereg.CultivateReg;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CultivateReg.MODID)
public class CultivationCapability {
    public static final Capability<CultivationData> CULTIVATION_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation KEY =
            ResourceLocation.fromNamespaceAndPath(CultivateReg.MODID, "cultivation");

    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final CultivationDataImpl backend = new CultivationDataImpl();
        private final LazyOptional<CultivationData> opt = LazyOptional.of(() -> backend);

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            return cap == CULTIVATION_CAP ? opt.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            // --- core ---
            tag.putInt("realm", backend.getRealm().ordinal());
            tag.putInt("stage", backend.getStage());
            tag.putFloat("qi", backend.getQi());
            tag.putBoolean("med", backend.isMeditating());
            tag.putBoolean("cultivationUnlocked", backend.isCultivationUnlocked());
            tag.putBoolean("sensed", backend.hasSensed());
            tag.putFloat("sensep", backend.getSenseProgress());
            tag.putFloat("spirit", backend.getSpirit()); // combat pool
            tag.putBoolean("rest", backend.isResting());
            tag.putString("manual", backend.getManualId().toString());
            tag.putInt("manualProg", backend.getManualQuizProgress());
            tag.putBoolean("manualPassed", backend.isManualQuizPassed());

            // --- meridians (mask + per-node progress) ---
            int mask = 0;
            CompoundTag mer = new CompoundTag();
            for (int i = 0; i < CultivationData.MERIDIANS; i++) {
                if (backend.isMeridianOpen(i)) mask |= (1 << i);
                mer.putInt("p" + i, backend.getMeridianProgress(i)); // 0..100
            }
            tag.putInt("merMask", mask);
            tag.put("merProg", mer);

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag == null) return;

            // --- core ---
            backend.setRealm(Realm.values()[tag.getInt("realm")]);
            backend.setStage(tag.getInt("stage"));
            backend.setQi(tag.getFloat("qi"));
            backend.setMeditating(false); // never load as actively meditating
            backend.setCultivationUnlocked(tag.getBoolean("cultivationUnlocked"));
            backend.setSensed(tag.getBoolean("sensed"));
            backend.setSenseProgress(tag.getFloat("sensep"));
            backend.setSpirit(tag.contains("spirit") ? tag.getFloat("spirit") : 0f);
            backend.setResting(false);
            if (tag.contains("manual")) {
                backend.setManualId(ResourceLocation.tryParse(tag.getString("manual")));
            }
            backend.setManualQuizProgress(tag.contains("manualProg") ? tag.getInt("manualProg") : 0);
            backend.setManualQuizPassed(tag.getBoolean("manualPassed"));

            // --- meridians ---
            int mask = tag.getInt("merMask");
            CompoundTag mer = tag.getCompound("merProg"); // may be empty on old saves
            for (int i = 0; i < CultivationData.MERIDIANS; i++) {
                boolean open = ((mask >> i) & 1) != 0;
                int prog = mer.contains("p" + i) ? mer.getInt("p" + i) : 0;
                backend.setMeridianOpen(i, open);
                backend.setMeridianProgress(i, prog);
            }
        }
    }

    @SubscribeEvent
    public static void attachCapabilities(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player) {
            e.addCapability(KEY, new Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        e.getOriginal().getCapability(CULTIVATION_CAP).ifPresent(oldCap -> {
            e.getEntity().getCapability(CULTIVATION_CAP).ifPresent(newCap -> {
                // --- core ---
                newCap.setRealm(oldCap.getRealm());
                newCap.setStage(oldCap.getStage());
                newCap.setQi(oldCap.getQi());
                newCap.setCultivationUnlocked(oldCap.isCultivationUnlocked());
                newCap.setSensed(oldCap.hasSensed());
                newCap.setSenseProgress(oldCap.getSenseProgress());
                newCap.setSpirit(oldCap.getSpirit()); // carry over combat pool
                newCap.setMeditating(false);
                if (newCap instanceof CultivationDataImpl impl) impl.setResting(false);
                newCap.setManualId(oldCap.getManualId());
                newCap.setManualQuizProgress(oldCap.getManualQuizProgress());
                newCap.setManualQuizPassed(oldCap.isManualQuizPassed());

                // --- meridians ---
                for (int i = 0; i < CultivationData.MERIDIANS; i++) {
                    newCap.setMeridianOpen(i, oldCap.isMeridianOpen(i));
                    newCap.setMeridianProgress(i, oldCap.getMeridianProgress(i));
                }
            });
        });
    }
}
