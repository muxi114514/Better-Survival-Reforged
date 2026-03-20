package com.chen1335.ultimateEnchantment.recipe;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.CraftingRecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NBTShapedRecipeBuilder extends CraftingRecipeBuilder {
    public NBTShapedRecipeBuilder(RecipeCategory pCategory, ItemStack result) {


    }

    public ItemStack result;



    public static class Result extends CraftingRecipeBuilder.CraftingResult {


        protected Result(CraftingBookCategory pCategory) {
            super(pCategory);
        }

        @Override
        public ResourceLocation getId() {
            return null;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return null;
        }

        @Override
        public @Nullable JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public @Nullable ResourceLocation getAdvancementId() {
            return null;
        }
    }
}
