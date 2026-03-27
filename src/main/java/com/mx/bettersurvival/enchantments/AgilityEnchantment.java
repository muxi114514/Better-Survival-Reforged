package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.UUID;

public class AgilityEnchantment extends Enchantment {

    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("e6107045-134f-4c54-a645-75c3ae5c7a27");

    public AgilityEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.ARMOR_LEGS, new EquipmentSlot[] { EquipmentSlot.LEGS });
    }

    public static void applySpeedModifier(LivingEntity entity, int level) {
        if (level > 0) {
            double d = 0.01 * level;
            AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed == null)
                return;

            AttributeModifier existing = speed.getModifier(SPEED_MODIFIER_UUID);
            if (existing == null) {
                speed.addTransientModifier(new AttributeModifier(
                        SPEED_MODIFIER_UUID, "agility", d, AttributeModifier.Operation.ADDITION));
            } else if (existing.getAmount() != d) {
                speed.removeModifier(SPEED_MODIFIER_UUID);
                speed.addTransientModifier(new AttributeModifier(
                        SPEED_MODIFIER_UUID, "agility", d, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    public static void removeSpeedModifier(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(SPEED_MODIFIER_UUID) != null) {
            speed.removeModifier(SPEED_MODIFIER_UUID);
        }
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.agilityLevel.get();
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
        return ModConfig.COMMON.agilityTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.agilityLevel.get() != 0;
    }
}
