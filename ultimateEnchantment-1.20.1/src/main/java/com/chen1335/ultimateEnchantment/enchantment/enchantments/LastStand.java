package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import com.chen1335.ultimateEnchantment.enchantment.EnchantmentUtils;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.EnumMap;
import java.util.UUID;

public class LastStand extends CommonEnchantmentBase {

    public static final EnumMap<ArmorItem.Type, UUID> ARMOR_MODIFIER_UUID_PER_TYPE = EnchantmentUtils.getRandomUUIDMap();

    public static final EnumMap<ArmorItem.Type, UUID> ARMOR_TOUGHNESS_MODIFIER_UUID_PER_TYPE = EnchantmentUtils.getRandomUUIDMap();

    public float armorBonusPerLevel = 0.05F;

    public float armorToughnessBonusPerLevel = 0.05F;

    public float maxHealthPercentage = 0.34F;

    public LastStand() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR, Enchantments.ARMOR_SLOTS, UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    public float getEffectiveMaximumHealthPercentage() {
        return maxHealthPercentage;
    }

    public float getArmorBonus(int level) {
        return armorBonusPerLevel * level;
    }

    public float getArmorToughnessBonus(int level) {
        return armorToughnessBonusPerLevel * level;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void loadConfig(Configuration config) {
        super.loadConfig(config);
        maxHealthPercentage = config.getFloat("maxHealthPercentage", this.getSimpleName(), maxHealthPercentage, Float.MIN_VALUE, Float.MAX_VALUE, "the maxHealthPercentage");
        armorBonusPerLevel = config.getFloat("armorBonusPerLevel", this.getSimpleName(), armorBonusPerLevel, Float.MIN_VALUE, Float.MAX_VALUE, "the armorBonusPerLevel");
        armorToughnessBonusPerLevel = config.getFloat("armorToughnessBonusPerLevel", this.getSimpleName(), armorToughnessBonusPerLevel, Float.MIN_VALUE, Float.MAX_VALUE, "the armorToughnessBonusPerLevel");
    }
}
