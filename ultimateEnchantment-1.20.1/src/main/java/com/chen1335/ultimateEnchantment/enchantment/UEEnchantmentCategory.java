package com.chen1335.ultimateEnchantment.enchantment;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class UEEnchantmentCategory {
    public static EnchantmentCategory CAN_ENCHANT = EnchantmentCategory.create("CAN_ENCHANT", item -> item.getMaxStackSize(item.getDefaultInstance()) == 1);

    public static EnchantmentCategory WEAPON_AND_BOW = EnchantmentCategory.create("WEAPON_AND_BOW", item ->
            EnchantmentCategory.WEAPON.canEnchant(item)
                    || EnchantmentCategory.TRIDENT.canEnchant(item)
                    || EnchantmentCategory.BOW.canEnchant(item)
                    || EnchantmentCategory.CROSSBOW.canEnchant(item)
                    || item instanceof AxeItem);

    public static EnchantmentCategory MELEE_WEAPON = EnchantmentCategory.create("MELEE_WEAPON", item ->
            EnchantmentCategory.WEAPON.canEnchant(item)
                    || EnchantmentCategory.TRIDENT.canEnchant(item)
                    || item instanceof AxeItem);
}
