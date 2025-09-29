package com.bo.cultivatereg.loot;

import com.bo.cultivatereg.cultivation.MobCultivationCapability;
import com.bo.cultivatereg.cultivation.MobCultivationData;
import com.bo.cultivatereg.cultivation.Realm;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import java.util.Optional;

public class AddSpiritStoneModifier extends LootModifier {
    public record TierEntry(Item item, int count) {
        public static final Codec<TierEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(TierEntry::item),
                Codec.INT.fieldOf("count").forGetter(TierEntry::count)
        ).apply(inst, TierEntry::new));
    }

    private final TierEntry low;
    private final TierEntry mid;
    private final TierEntry high;
    private final TierEntry top;
    private final TierEntry nascent;
    private final TierEntry soul;
    private final TierEntry spirit;
    private final TierEntry voidRefining;
    private final TierEntry integration;
    private final TierEntry tribulation;
    private final float coreTopChance;

    public static final Codec<AddSpiritStoneModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst)
                    .and(TierEntry.CODEC.fieldOf("low").forGetter(m -> m.low))
                    .and(TierEntry.CODEC.fieldOf("mid").forGetter(m -> m.mid))
                    .and(TierEntry.CODEC.fieldOf("high").forGetter(m -> m.high))
                    .and(TierEntry.CODEC.fieldOf("top").forGetter(m -> m.top))
                    .and(TierEntry.CODEC.optionalFieldOf("nascent").forGetter(m -> Optional.ofNullable(m.nascent)))
                    .and(TierEntry.CODEC.optionalFieldOf("soul").forGetter(m -> Optional.ofNullable(m.soul)))
                    .and(TierEntry.CODEC.optionalFieldOf("spirit").forGetter(m -> Optional.ofNullable(m.spirit)))
                    .and(TierEntry.CODEC.optionalFieldOf("void").forGetter(m -> Optional.ofNullable(m.voidRefining)))
                    .and(TierEntry.CODEC.optionalFieldOf("integration").forGetter(m -> Optional.ofNullable(m.integration)))
                    .and(TierEntry.CODEC.optionalFieldOf("tribulation").forGetter(m -> Optional.ofNullable(m.tribulation)))
                    .and(Codec.FLOAT.optionalFieldOf("core_top_chance", 0.05f).forGetter(m -> m.coreTopChance))
                    .apply(inst, AddSpiritStoneModifier::new)
    );

    protected AddSpiritStoneModifier(
            LootItemCondition[] conditions,
            TierEntry low,
            TierEntry mid,
            TierEntry high,
            TierEntry top,
            Optional<TierEntry> nascent,
            Optional<TierEntry> soul,
            Optional<TierEntry> spirit,
            Optional<TierEntry> voidRefining,
            Optional<TierEntry> integration,
            Optional<TierEntry> tribulation,
            float coreTopChance
    ) {
            super(conditions);
            this.low = low;
            this.mid = mid;
            this.high = high;
            this.top = top;
        this.nascent = nascent.orElse(null);
        this.soul = soul.orElse(null);
        this.spirit = spirit.orElse(null);
        this.voidRefining = voidRefining.orElse(null);
        this.integration = integration.orElse(null);
        this.tribulation = tribulation.orElse(null);
            this.coreTopChance = Math.max(0.0f, coreTopChance);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
            Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (!(entity instanceof LivingEntity living)) {
                addIfPresent(generatedLoot, low);
                return generatedLoot;
            }

            Optional<MobCultivationData> dataOpt = living.getCapability(MobCultivationCapability.CAP).resolve();
            if (dataOpt.isEmpty() || !dataOpt.get().hasCultivation()) {
                addIfPresent(generatedLoot, low);
                return generatedLoot;
            }

            MobCultivationData data = dataOpt.get();
            Realm realm = data.getRealm();

            if (realm == Realm.FOUNDATION) {
                addIfPresent(generatedLoot, mid);
            } else if (realm == Realm.CORE_FORMATION) {
                addIfPresent(generatedLoot, high);
                if (coreTopChance > 0f && ctx.getRandom().nextFloat() < coreTopChance) {
                    addIfPresent(generatedLoot, top);
                }
            } else if (realm == Realm.NASCENT_SOUL) {
                if (!addIfPresent(generatedLoot, nascent)) {
                    addIfPresent(generatedLoot, top);
                }
            } else if (realm == Realm.SOUL_TRANSFORMATION) {
                if (!addIfPresent(generatedLoot, soul)) {
                    addIfPresent(generatedLoot, nascent);
                }
            } else if (realm == Realm.SPIRIT_SEVERING) {
                if (!addIfPresent(generatedLoot, spirit)) {
                    addIfPresent(generatedLoot, soul);
                }
            } else if (realm == Realm.VOID_REFINING) {
                if (!addIfPresent(generatedLoot, voidRefining)) {
                    addIfPresent(generatedLoot, spirit);
                }
            } else if (realm == Realm.INTEGRATION) {
                if (!addIfPresent(generatedLoot, integration)) {
                    addIfPresent(generatedLoot, voidRefining);
                }
            } else if (realm == Realm.TRIBULATION) {
                if (!addIfPresent(generatedLoot, tribulation)) {
                    addIfPresent(generatedLoot, integration);
                }
            } else {
                // Qi Gathering or anything else defaults to low tier
                addIfPresent(generatedLoot, low);
            }

            return generatedLoot;
        }

    private static boolean addIfPresent(ObjectArrayList<ItemStack> generatedLoot, TierEntry entry) {
        if (entry == null) return false;
            Item item = entry.item();
            int count = entry.count();
        if (item == null || count <= 0) return false;
            generatedLoot.add(new ItemStack(item, count));
        return true;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }