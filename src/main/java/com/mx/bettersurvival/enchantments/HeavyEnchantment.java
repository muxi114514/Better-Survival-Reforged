package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/** 负重：主动格挡时获得抗击退（与 Weightless 互斥）。 */
public class HeavyEnchantment extends Enchantment {

    public HeavyEnchantment() {
        super(Rarity.RARE, CustomShieldItem.SHIELD_CATEGORY,
                new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.heavyLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != ModEnchantments.WEIGHTLESS.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.heavyTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.heavyLevel.get() != 0;
    }
}
