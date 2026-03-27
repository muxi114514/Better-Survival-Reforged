package com.mx.bettersurvival.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ChestLootModifier extends LootModifier {

    public static final Supplier<Codec<ChestLootModifier>> CODEC = Suppliers
            .memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst)
                    .and(ResourceLocation.CODEC.fieldOf("loot_table").forGetter(m -> m.lootTable))
                    .apply(inst, ChestLootModifier::new)));

    private final ResourceLocation lootTable;

    public ChestLootModifier(LootItemCondition[] conditions, ResourceLocation lootTable) {
        super(conditions);
        this.lootTable = lootTable;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
            LootContext context) {
        LootTable subTable = context.getResolver().getLootTable(lootTable);
        subTable.getRandomItemsRaw(context, generatedLoot::add);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
