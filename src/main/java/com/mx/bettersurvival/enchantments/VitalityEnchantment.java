package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.GameRules;

public class VitalityEnchantment extends Enchantment {

    public VitalityEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[] { EquipmentSlot.CHEST });
    }

    /**
     * Called during LivingEvent.LivingTickEvent to heal the player passively.
     */
    public static void healPlayer(Player player, int level) {
        if (level > 0
                && player.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION)
                && player.getFoodData().getFoodLevel() >= 18
                && player.getHealth() < player.getMaxHealth()
                && player.tickCount % (40 / level) == 0) {
            player.heal(1.0F);
        }
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.vitalityLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 20 + 15 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 40;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.vitalityTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.vitalityLevel.get() != 0;
    }
}
