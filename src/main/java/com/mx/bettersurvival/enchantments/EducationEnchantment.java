package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public class EducationEnchantment extends Enchantment {

    public EducationEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    /**
     * Called during LivingExperienceDropEvent if enemy is killed by an attacker
     * with enchanted weapon.
     */
    public static float getExpMultiplier(Player killer, LivingEntity killed) {
        if (!killed.shouldDropExperience())
            return 1.0F;
        float lvl = (float) EnchantmentHelper.getEnchantmentLevel(ModEnchantments.EDUCATION.get(), killer);
        return (lvl / 2.0F) + 1.0F;
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.educationLevel.get();
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
    protected boolean checkCompatibility(Enchantment other) {
        return super.checkCompatibility(other) && other != Enchantments.MOB_LOOTING;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.educationTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.educationLevel.get() != 0;
    }
}
