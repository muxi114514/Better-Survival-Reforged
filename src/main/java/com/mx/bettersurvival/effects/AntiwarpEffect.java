package com.mx.bettersurvival.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class AntiwarpEffect extends MobEffect {

    public AntiwarpEffect() {
        super(MobEffectCategory.HARMFUL, 0xDB6B58);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
