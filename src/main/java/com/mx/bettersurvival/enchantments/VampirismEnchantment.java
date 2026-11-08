package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
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

    /**
     * Called from CommonEventHandler.onLivingHurt for direct melee hits with the main-hand weapon.
     *
     * <p>This used to live in {@code doPostAttack}, but vanilla's {@code EnchantmentHelper.doPostDamageEffects}
     * runs that hook for every equipped item (so a Vampirism sword in the off hand healed on any hit, arrows
     * included) and, for players, runs it a second time for the main hand — healing rolled twice per hit.
     */
    public static void tryHeal(LivingEntity user, int level) {
        if (level > 0 && user.getRandom().nextFloat() < (float) level * 0.2F) {
            user.heal(Math.max(1.0F, (float) level * 0.2F));
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
