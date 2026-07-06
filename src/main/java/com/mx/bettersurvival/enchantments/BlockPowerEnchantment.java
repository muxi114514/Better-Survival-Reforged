package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/** 格挡强化：提高主动格挡时挡下的伤害比例（与 SpellShield 互斥）。 */
public class BlockPowerEnchantment extends Enchantment {

    public BlockPowerEnchantment() {
        super(Rarity.COMMON, CustomShieldItem.SHIELD_CATEGORY,
                new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.blockingPowerLevel.get();
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
        return super.checkCompatibility(other) && other != ModEnchantments.SPELL_SHIELD.get();
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.blockingPowerTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.blockingPowerLevel.get() != 0;
    }
}
