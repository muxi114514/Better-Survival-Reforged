package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.SingleAttributeEnchantment;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class QuickShooting extends SingleAttributeEnchantment {
    public QuickShooting() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.LEGENDARY_ENCHANTMENT, ALObjects.Attributes.DRAW_SPEED.get(), 0.1F, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 80 + 10 * pLevel;
    }

    @Override
    public int getMinCost(int pLevel) {
        return 30 + 10 * pLevel;
    }
}
