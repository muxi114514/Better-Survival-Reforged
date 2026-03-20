package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VampirismEnchantment extends Enchantment {

    public VampirismEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.vampirismLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 10 + 15 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if (user != null && target != null && !user.level().isClientSide) {
            if (user.getRandom().nextFloat() < (float) level * 0.2F) {
                user.heal(Math.max(1.0F, (float) level * 0.2F));
            }
        }
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.vampirismTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.vampirismLevel.get() != 0;
    }
}
