package com.chen1335.ultimateEnchantment.data;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class LootProvider extends LootTableProvider {
    public static ResourceLocation WITHER_LOOT_ADDITION = new ResourceLocation(UltimateEnchantment.MODID, "wither_loot_addition");
    public static ResourceLocation ENDER_DRAGON_LOOT_ADDITION = new ResourceLocation(UltimateEnchantment.MODID, "ender_dragon_loot_addition");
    public static ResourceLocation WARDEN_LOOT_ADDITION = new ResourceLocation(UltimateEnchantment.MODID, "warden_loot_addition");


    public LootProvider(PackOutput pOutput, Set<ResourceLocation> pRequiredTables) {
        super(pOutput, pRequiredTables, List.of(new SubProviderEntry(AdditionLoot::new, LootContextParamSets.ENTITY)));
    }

    public static class AdditionLoot implements LootTableSubProvider {


        @Override
        public void generate(@NotNull BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
            biConsumer.accept(WITHER_LOOT_ADDITION,
                    LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                            LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                    .apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.LEGEND.get(), ConstantValue.exactly(1.0F)))
                                    .setWeight(1)
                    ))
            );
            biConsumer.accept(ENDER_DRAGON_LOOT_ADDITION,
                    LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                            LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                    .apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.ULTIMATE.get(), ConstantValue.exactly(2.0F)))
                                    .setWeight(1)
                    ))
            );
            biConsumer.accept(WARDEN_LOOT_ADDITION,
                    LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(
                            LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                                    .apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.LAST_STAND.get(), ConstantValue.exactly(1.0F)))
                                    .setWeight(1)
                    ))
            );
        }
    }
}
