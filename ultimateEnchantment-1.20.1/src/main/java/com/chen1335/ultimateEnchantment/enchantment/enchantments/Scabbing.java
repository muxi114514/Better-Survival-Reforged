package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.enchantment.SingleAttributeEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class Scabbing extends SingleAttributeEnchantment {
    public Scabbing() {
        super(Rarity.RARE, UEEnchantmentCategory.MELEE_WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.LEGENDARY_ENCHANTMENT, ALObjects.Attributes.ARMOR_SHRED.get(), 0.05F, AttributeModifier.Operation.ADDITION);
    }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment pOther) {
        return pOther != Enchantments.PIERCE_THROUGH.get();
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
        return 20 + 10 * pLevel;
    }
}
