package com.mx.bettersurvival.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StunEffect extends MobEffect {

    private static final String STUN_SLOWDOWN_UUID = "7107DE5E-7CE8-4030-940E-514C1F160890";

    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0x4E9331);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, STUN_SLOWDOWN_UUID,
                -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, "55FCED67-E92A-486E-9580-6F44D54A36E6",
                -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
