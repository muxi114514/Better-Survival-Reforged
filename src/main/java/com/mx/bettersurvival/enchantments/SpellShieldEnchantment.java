package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

/** 法术护盾：举盾时部分抵挡魔法伤害（magic/indirect_magic 这类绕盾伤害）。 */
public class SpellShieldEnchantment extends Enchantment {

    public SpellShieldEnchantment() {
        super(Rarity.UNCOMMON, CustomShieldItem.SHIELD_CATEGORY,
                new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.spellShieldLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.spellShieldTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.spellShieldLevel.get() != 0;
    }
}
