package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;

public class CutDown extends CommonEnchantmentBase {

    public float maxDamageBonusPerLevel = 0.1F;

    public float damageBonusPerExceedHealthPercentage = 0.0001F;

    public CutDown() {
        super(Rarity.RARE, UEEnchantmentCategory.MELEE_WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.LEGENDARY_ENCHANTMENT);
    }

    public float getDamageBonus(float exceedHealthPercentage, int level) {
        return Math.min(damageBonusPerExceedHealthPercentage * exceedHealthPercentage * level, maxDamageBonusPerLevel * level);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 80 * 10 + pLevel;
    }

    @Override
    public int getMinCost(int pLevel) {
        return 30 + 10 * pLevel;
    }
}
