package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class VersatilityEnchantment extends Enchantment {

    public VersatilityEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.DIGGER, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    // 直接用 BreakSpeed 事件已算好的基础速度；切勿在此再调 getDestroySpeed，
    // 否则会重入"手持工具/方块所属模组"的速度计算，遇到会重触发 BreakSpeed 的模组即无限递归崩端。
    public static float getSpeedModifier(Player miner, float originalSpeed) {
        ItemStack stack = miner.getMainHandItem();

        if (originalSpeed <= 1.0F && stack.getItem() instanceof DiggerItem digger) {
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
