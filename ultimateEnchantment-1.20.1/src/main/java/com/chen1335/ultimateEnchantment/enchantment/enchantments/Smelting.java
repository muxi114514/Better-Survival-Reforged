package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Smelting extends Enchantment {
    public Smelting() {
        super(Rarity.COMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    public static List<ItemStack> transformDropsAndDropExp(List<ItemStack> drops, BlockPos blockPos, ItemStack tool, ServerLevel serverLevel) {
        AtomicReference<Float> expValue = new AtomicReference<>((float) 0);
        for (int i = 0; i < drops.size(); i++) {
            ItemStack itemStack = drops.get(i);
            int finalI = i;
            serverLevel.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream().filter(smeltingRecipe -> {
                for (int j = 0; j < smeltingRecipe.getIngredients().size(); j++) {
                    if (smeltingRecipe.getIngredients().get(j).test(itemStack)) {
                        return true;
                    }
                }
                return false;
            }).findFirst().ifPresent(smeltingRecipe -> {
                ItemStack result = smeltingRecipe.getResultItem(serverLevel.registryAccess()).copy();
                result.setCount(result.getCount() * itemStack.getCount());
                drops.set(finalI, result);
                expValue.updateAndGet(v -> v + smeltingRecipe.getExperience());
            });
        }

        int i = Mth.floor(expValue.get());
        float f = Mth.frac(expValue.get());
        if (f != 0.0F && Math.random() < (double) f) {
            ++i;
        }
        if (i > 0) {
            serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), i));
        }
        return drops;
    }
}
