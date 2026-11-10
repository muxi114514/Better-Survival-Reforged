package com.mx.bettersurvival.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.brewing.IBrewingRecipe;

/**
 * Brewing recipe that matches the input by its potion type. A Forge BrewingRecipe
 * built from Ingredient.of(potionStack) only compares the item, not the potion NBT,
 * so every potion bottle (even water) was accepted as input. Works for any potion
 * item (normal, splash, lingering) and keeps the input's item in the output.
 */
public class PotionTypeBrewingRecipe implements IBrewingRecipe {

    private final Potion input;
    private final Ingredient reagent;
    private final Potion output;

    public PotionTypeBrewingRecipe(Potion input, ItemLike reagent, Potion output) {
        this.input = input;
        this.reagent = Ingredient.of(reagent);
        this.output = output;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return stack.getItem() instanceof PotionItem && PotionUtils.getPotion(stack) == input;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return reagent.test(stack);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (!isInput(input) || !isIngredient(ingredient))
            return ItemStack.EMPTY;
        return PotionUtils.setPotion(new ItemStack(input.getItem()), output);
    }
}
