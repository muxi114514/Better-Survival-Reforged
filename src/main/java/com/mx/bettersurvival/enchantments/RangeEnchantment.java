package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class RangeEnchantment extends Enchantment {

    public RangeEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    /**
     * Returns the velocity multiplier for Range enchanted arrows.
     */
    public static double getVelocityMultiplier() {
        return ModConfig.COMMON.rangeVelocity.get();
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.rangeLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.rangeTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.rangeLevel.get() != 0;
    }
}
