package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

public class VersatilityEnchantment extends Enchantment {

    public VersatilityEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    /**
     * Called during PlayerEvent.BreakSpeed to give a speed boost when the tool is
     * not effective.
     * Returns the modified mining speed, or the original speed if no boost is
     * needed.
     */
    public static float getSpeedModifier(Player miner, BlockState state) {
        ItemStack stack = miner.getMainHandItem();
        // If the tool is not effective on the block, give half the tool's base speed
        if (miner.getDestroySpeed(state) <= 1.0F && stack.getItem() instanceof DiggerItem digger) {
            return digger.getTier().getSpeed() / 2.0F;
        }
        return 1.0F;
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.versatilityLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return level * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.versatilityTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.versatilityLevel.get() != 0;
    }
}
