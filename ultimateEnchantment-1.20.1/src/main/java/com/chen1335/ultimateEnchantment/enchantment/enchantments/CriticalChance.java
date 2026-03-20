package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.SingleAttributeEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class CriticalChance extends SingleAttributeEnchantment {


    public CriticalChance() {
        super(Rarity.COMMON, UEEnchantmentCategory.WEAPON_AND_BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.OTHER, ALObjects.Attributes.CRIT_CHANCE.get(), 0.02F, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 80 + 10 * pLevel;
    }

    @Override
    public int getMinCost(int pLevel) {
        return 20 + 10 * pLevel;
    }
}
