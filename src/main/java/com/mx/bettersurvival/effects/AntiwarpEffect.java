package com.mx.bettersurvival.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Antiwarp effect – marker effect that prevents Enderman teleportation.
 * The actual teleport prevention is handled in CommonEventHandler via
 * EntityTeleportEvent.EnderEntity.
 */
public class AntiwarpEffect extends MobEffect {

    public AntiwarpEffect() {
        super(MobEffectCategory.HARMFUL, 0xDB6B58); // reddish
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // No tick logic – this is a pure marker effect
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
