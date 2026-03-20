package com.chen1335.ultimateEnchantment.data;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class UERecipeProvider extends RecipeProvider {
    public UERecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        ItemStack enchantedBookItem = Items.ENCHANTED_BOOK.getDefaultInstance();
        enchantedBookItem.enchant(Enchantments.MENDING, 1);

//        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, Items.GRANITE, 16)
//                .define('#', PartialNBTIngredient.of(enchantedBookItem.getOrCreateTag(), enchantedBookItem.getItem()))
//                .pattern("###")
//                .pattern("###")
//                .group("stained_glass_pane")
//                .unlockedBy("has_glass", has(Items.GRANITE))
//                .save(pWriter);

    }
}
