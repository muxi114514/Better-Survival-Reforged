package com.chen1335.ultimateEnchantment.enchantment;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.config.EnchantmentConfig;
import com.chen1335.ultimateEnchantment.enchantment.config.SimpleEnchantmentInfo;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IEnchantmentExtension;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.jetbrains.annotations.NotNull;

public class CommonEnchantmentBase extends Enchantment {

    private SimpleEnchantmentInfo info;

    public CommonEnchantmentBase(Rarity pRarity, EnchantmentCategory pCategory, EquipmentSlot[] pApplicableSlots, UltimateEnchantment.EnchantmentType enchantmentType) {
        super(pRarity, pCategory, pApplicableSlots);
        ((IEnchantmentExtension) this).ue$setEnchantmentType(enchantmentType);
        EnchantmentConfig.sets.add(this);
    }

    public void loadConfig(Configuration config) {
        info = new SimpleEnchantmentInfo(this, config);
    }


    @Override
    public boolean isTradeable() {
        if (info != null) {
            return info.isTradeable;
        }
        return !(((IEnchantmentExtension) this).ue$getEnchantmentType() == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    @Override
    public boolean isTreasureOnly() {
        if (info != null) {
            return info.isTreasureOnly;
        }
        return (((IEnchantmentExtension) this).ue$getEnchantmentType() == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    @Override
    public boolean isDiscoverable() {
        if (info != null) {
            return info.isDiscoverable;
        }
        return !(((IEnchantmentExtension) this).ue$getEnchantmentType() == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment pOther) {
        UltimateEnchantment.EnchantmentType type = ((IEnchantmentExtension) this).ue$getEnchantmentType();
        if (type == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT) {
            return type != ((IEnchantmentExtension) pOther).ue$getEnchantmentType();
        }
        return true;
    }

    public String getSimpleName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getMinCost(int pLevel) {
        UltimateEnchantment.EnchantmentType type = ((IEnchantmentExtension) this).ue$getEnchantmentType();
        if (type == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT) {
            return 120;
        }
        return super.getMinCost(pLevel);
    }

    public SimpleEnchantmentInfo getInfo() {
        return info;
    }

    public void setInfo(SimpleEnchantmentInfo info) {
        this.info = info;
    }
}
