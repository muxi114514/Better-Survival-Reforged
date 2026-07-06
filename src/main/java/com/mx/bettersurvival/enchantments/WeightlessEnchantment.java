package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/** 轻量：减轻盾牌重量，降低格挡时的移速惩罚（与 Heavy 互斥）。 */
public class WeightlessEnchantment extends Enchantment {

    public WeightlessEnchantment() {
        super(Rarity.UNCOMMON, CustomShieldItem.SHIELD_CATEGORY,
                new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.weightlessLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 8;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.HEAVY.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.weightlessTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.weightlessLevel.get() != 0;
    }
}
