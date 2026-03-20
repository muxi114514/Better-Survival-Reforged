package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Legend extends CommonEnchantmentBase {

    public float attributeBonusPerLevel = 0.02F;

    public static final Set<Attribute> BLACK_LIST = new HashSet<>();

    public Legend() {
        super(Rarity.VERY_RARE, UEEnchantmentCategory.CAN_ENCHANT, EquipmentSlot.values(), UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
    }

    public static void buildBlackList(List<String> legendBlackList) {
        legendBlackList.forEach(s -> {
            ForgeRegistries.ATTRIBUTES.getHolder(ResourceLocation.tryParse(s)).ifPresent(attributeHolder -> {
                BLACK_LIST.add(attributeHolder.value());
            });
        });
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    public float getAttributeBonus(int level) {
        return attributeBonusPerLevel * level;
    }

    @Override
    public void loadConfig(Configuration config) {
        super.loadConfig(config);
        attributeBonusPerLevel = config.getFloat("attributeBonusPerLevel", this.getSimpleName(), attributeBonusPerLevel, Float.MIN_VALUE, Float.MAX_VALUE, "the attributeBonusPerLevel");
    }
}
