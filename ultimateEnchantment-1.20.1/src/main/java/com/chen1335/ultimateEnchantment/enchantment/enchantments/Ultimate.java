package com.chen1335.ultimateEnchantment.enchantment.enchantments;


import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import net.minecraft.world.entity.EquipmentSlot;

public class Ultimate extends CommonEnchantmentBase {
    public Ultimate() {
        super(Rarity.VERY_RARE, UEEnchantmentCategory.CAN_ENCHANT, EquipmentSlot.values(), UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

}
