package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.items.BattleAxeItem;
import com.mx.bettersurvival.items.DaggerItem;
import com.mx.bettersurvival.items.HammerItem;
import com.mx.bettersurvival.items.NunchakuItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class WeaponSpecialEnchantment extends Enchantment {

    private final WeaponType weaponType;

    public WeaponSpecialEnchantment(WeaponType weaponType) {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        this.weaponType = weaponType;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    @Override
    public int getMinCost(int level) {
        return 5 + (level - 1) * 9;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 15;
    }

    @Override
    public int getMaxLevel() {
        return switch (weaponType) {
            case DAGGER -> ModConfig.COMMON.assassinateLevel.get();
            case NUNCHAKU -> ModConfig.COMMON.comboLevel.get();
            case HAMMER -> ModConfig.COMMON.bashLevel.get();
            case BATTLEAXE -> ModConfig.COMMON.disarmLevel.get();
        };
    }

    @Override
    public boolean isAllowedOnBooks() {
        return getMaxLevel() != 0;
    }

    @Override
    public boolean isTreasureOnly() {
        return switch (weaponType) {
            case DAGGER -> ModConfig.COMMON.assassinateTreasure.get();
            case NUNCHAKU -> ModConfig.COMMON.comboTreasure.get();
            case HAMMER -> ModConfig.COMMON.bashTreasure.get();
            case BATTLEAXE -> ModConfig.COMMON.disarmTreasure.get();
        };
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return switch (weaponType) {
            case DAGGER -> stack.getItem() instanceof DaggerItem;
            case NUNCHAKU -> stack.getItem() instanceof NunchakuItem;
            case HAMMER -> stack.getItem() instanceof HammerItem;
            case BATTLEAXE -> stack.getItem() instanceof BattleAxeItem;
        };
    }

    public enum WeaponType {
        DAGGER,
        NUNCHAKU,
        BATTLEAXE,
        HAMMER
    }
}
