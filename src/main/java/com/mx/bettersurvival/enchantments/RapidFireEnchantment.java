package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RapidFireEnchantment extends Enchantment {

    public RapidFireEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    public static int getChargeTimeReduction(LivingEntity shooter, int charge) {
        int level = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.RAPID_FIRE.get(), shooter);
        if (level < 4) {
            return (charge % (5 - level) == 0) ? 1 : 0;
        } else {
            return level - 3;
        }
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.rapidFireLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 15 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.rapidFireTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.rapidFireLevel.get() != 0;
    }
}
