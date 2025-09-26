package com.bo.cultivatereg.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AddSpiritStoneModifier extends LootModifier {
    private final ItemStack stack;

    public static final Codec<AddSpiritStoneModifier> CODEC = RecordCodecBuilder.create(inst ->
            LootModifier.codecStart(inst)
                    .and(ItemStack.CODEC.fieldOf("stack").forGetter(m -> m.stack))
                    .apply(inst, AddSpiritStoneModifier::new)
    );

    protected AddSpiritStoneModifier(LootItemCondition[] conditions, ItemStack stack) {
        super(conditions);
        this.stack = stack;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        generatedLoot.add(stack.copy());
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
