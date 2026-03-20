package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;

public class ArrowRecoveryEnchantment extends Enchantment {

    public ArrowRecoveryEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.BOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.arrowRecoveryLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != Enchantments.INFINITY_ARROWS;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.arrowRecoveryTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.arrowRecoveryLevel.get() != 0;
    }
}
