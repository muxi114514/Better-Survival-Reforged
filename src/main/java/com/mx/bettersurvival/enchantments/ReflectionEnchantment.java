package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** 反射：主动格挡近战时，概率将伤害反弹给攻击者。 */
public class ReflectionEnchantment extends Enchantment {

    public ReflectionEnchantment() {
        super(Rarity.RARE, CustomShieldItem.SHIELD_CATEGORY,
                new EquipmentSlot[] { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND });
    }

    /** 格挡近战成功时调用：按等级概率反弹荆棘伤害，并消耗盾牌 1 点耐久。 */
    public static void reflectDamage(Entity attacker, LivingEntity defender, ItemStack shield) {
        int level = shield.getEnchantmentLevel(ModEnchantments.REFLECTION.get());
        if (level <= 0) {
            return;
        }
        if (defender.getRandom().nextFloat() < 0.15F * level) {
            int dmg = level > 10 ? level - 10 : 1 + defender.getRandom().nextInt(4);
            attacker.hurt(defender.damageSources().thorns(defender), dmg);
            shield.hurtAndBreak(1, defender, e -> e.broadcastBreakEvent(defender.getUsedItemHand()));
        }
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.reflectionLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 10 + 20 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.reflectionTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.reflectionLevel.get() != 0;
    }
}
