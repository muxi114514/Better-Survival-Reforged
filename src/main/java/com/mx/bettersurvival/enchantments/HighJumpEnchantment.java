package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.phys.Vec3;

public class HighJumpEnchantment extends Enchantment {

    public HighJumpEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[] { EquipmentSlot.FEET });
    }

    public static void boostJump(LivingEntity jumper, int level) {
        Vec3 motion = jumper.getDeltaMovement();
        jumper.setDeltaMovement(motion.x, motion.y + ((double) level) / 10.0D, motion.z);
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.highJumpLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 10 + 20 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.highJumpTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.highJumpLevel.get() != 0;
    }
}
