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
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AddSpiritStoneModifier extends LootModifier {
    public record TierEntry(Item item, int count) {
        public static final Codec<TierEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(TierEntry::item),
                Codec.INT.fieldOf("count").forGetter(TierEntry::count)
        ).apply(inst, TierEntry::new));
    }

    private record ModifierSettings(
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
    }
    public static final Codec<AddSpiritStoneModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst)
                    .and(inst.group(
                            TierEntry.CODEC.fieldOf("low").forGetter(AddSpiritStoneModifier::lowEntry),
                            TierEntry.CODEC.fieldOf("mid").forGetter(AddSpiritStoneModifier::midEntry),
                            TierEntry.CODEC.fieldOf("high").forGetter(AddSpiritStoneModifier::highEntry),
                            TierEntry.CODEC.fieldOf("top").forGetter(AddSpiritStoneModifier::topEntry),
                            TierEntry.CODEC.optionalFieldOf("nascent").forGetter(AddSpiritStoneModifier::optionalNascent),
                            TierEntry.CODEC.optionalFieldOf("soul").forGetter(AddSpiritStoneModifier::optionalSoul),
                            TierEntry.CODEC.optionalFieldOf("spirit").forGetter(AddSpiritStoneModifier::optionalSpirit),
                            TierEntry.CODEC.optionalFieldOf("void_refining").forGetter(AddSpiritStoneModifier::optionalVoidRefining),
                            TierEntry.CODEC.optionalFieldOf("integration").forGetter(AddSpiritStoneModifier::optionalIntegration),
                            TierEntry.CODEC.optionalFieldOf("tribulation").forGetter(AddSpiritStoneModifier::optionalTribulation),
                            Codec.FLOAT.fieldOf("core_top_chance").forGetter(AddSpiritStoneModifier::coreTopChance)
                    ).apply(inst, ModifierSettings::new))
                    .apply(inst, (conditions, settings) -> new AddSpiritStoneModifier(
                            conditions,
                            settings.low(),
                            settings.mid(),
                            settings.high(),
                            settings.top(),
                            settings.nascent(),
                            settings.soul(),
                            settings.spirit(),
                            settings.voidRefining(),
                            settings.integration(),
                            settings.tribulation(),
                            settings.coreTopChance()
                    ))
    );

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
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
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

        switch (realm) {
            case FOUNDATION -> addIfPresent(generatedLoot, mid);
            case CORE_FORMATION -> {
                addIfPresent(generatedLoot, high);
                if (coreTopChance > 0f && ctx.getRandom().nextFloat() < coreTopChance) {
                    addIfPresent(generatedLoot, top);
                }
            }
            case NASCENT_SOUL -> {
                if (!addIfPresent(generatedLoot, nascent)) {
                    addIfPresent(generatedLoot, top);
                }
            }
            case SOUL_TRANSFORMATION -> {
                if (!addIfPresent(generatedLoot, soul)) {
                    addIfPresent(generatedLoot, nascent);
                }
            }
            case SPIRIT_SEVERING -> {
                if (!addIfPresent(generatedLoot, spirit)) {
                    addIfPresent(generatedLoot, soul);
                }
            }
            case VOID_REFINING -> {
                if (!addIfPresent(generatedLoot, voidRefining)) {
                    addIfPresent(generatedLoot, spirit);
                }
            }
            case INTEGRATION -> {
                if (!addIfPresent(generatedLoot, integration)) {
                    addIfPresent(generatedLoot, voidRefining);
                }
            }
            case TRIBULATION -> {
                if (!addIfPresent(generatedLoot, tribulation)) {
                    addIfPresent(generatedLoot, integration);
                }
            }
            default -> // Qi Gathering or anything else defaults to low tier
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

    // Helper methods for codec getters
    private TierEntry lowEntry() {
        return low;
    }
    private TierEntry midEntry() {
        return mid;
    }
    private TierEntry highEntry() {
        return high;
    }
    private TierEntry topEntry() {
        return top;
    }
    private Optional<TierEntry> optionalNascent() {
        return Optional.ofNullable(nascent);
    }
    private Optional<TierEntry> optionalSoul() {
        return Optional.ofNullable(soul);
    }
    private Optional<TierEntry> optionalSpirit() {
        return Optional.ofNullable(spirit);
    }
    private Optional<TierEntry> optionalVoidRefining() {
        return Optional.ofNullable(voidRefining);
    }
    private Optional<TierEntry> optionalIntegration() {
        return Optional.ofNullable(integration);
    }
    private Optional<TierEntry> optionalTribulation() {
        return Optional.ofNullable(tribulation);
    }
    private float coreTopChance() {
        return coreTopChance;
    }
}

