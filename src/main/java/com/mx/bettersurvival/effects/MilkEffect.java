package com.mx.bettersurvival.effects;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

/**
 * Milk effect – instant, clears all curable potion effects (same as drinking
 * milk).
 */
public class MilkEffect extends InstantenousMobEffect {

    public MilkEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF); // white
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource,
            LivingEntity target, int amplifier, double health) {
        target.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
    }
}
