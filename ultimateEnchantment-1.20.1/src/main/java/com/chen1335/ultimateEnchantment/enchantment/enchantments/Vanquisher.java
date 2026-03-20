package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.effect.MobEffects;
import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import com.chen1335.ultimateEnchantment.enchantment.UEEnchantmentCategory;
import com.chen1335.ultimateEnchantment.enchantment.config.SimpleEnchantmentInfo;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class Vanquisher extends CommonEnchantmentBase {
    public int buffDuration = 400;

    public Vanquisher() {
        super(Rarity.VERY_RARE, UEEnchantmentCategory.MELEE_WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT);
        this.setInfo(new SimpleEnchantmentInfo(false, false, true));
    }

    @Override
    public void doPostAttack(@NotNull LivingEntity pAttacker, @NotNull Entity pTarget, int pLevel) {
        super.doPostAttack(pAttacker, pTarget, pLevel);

        boolean checkSuccess = true;

        if (pAttacker instanceof Player player) {
            checkSuccess = player.getAttackStrengthScale(0.5F) >= 0.5;
        }

        if (checkSuccess) {
            int amplifier = 0;
            MobEffectInstance unActiveVanquisher = pAttacker.getEffect(MobEffects.UN_ACTIVE_VANQUISHER.get());
            if (unActiveVanquisher != null) {
                amplifier = unActiveVanquisher.getAmplifier() + 1;
            }
            pAttacker.addEffect(new MobEffectInstance(MobEffects.UN_ACTIVE_VANQUISHER.get(), buffDuration, amplifier, false, false, true));
            MobEffectInstance activeVanquisher = pAttacker.getEffect(MobEffects.ACTIVE_VANQUISHER.get());

            if (activeVanquisher != null) {
                pTarget.invulnerableTime = 0;
            }
        }
    }

    @Override
    public int getMinCost(int pLevel) {
        return 50;
    }

}
