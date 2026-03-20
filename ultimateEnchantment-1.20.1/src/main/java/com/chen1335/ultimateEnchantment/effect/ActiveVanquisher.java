package com.chen1335.ultimateEnchantment.effect;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class ActiveVanquisher extends MobEffect {
    public static final UUID LIFE_STEAL_MODIFIER_UUID = UUID.fromString("2c932783-b925-4f03-b244-9d3b436d4ced");
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("58c8b552-c0ab-44a0-9273-6e7141747248");
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("0d14ddab-f16d-4287-9b76-7b2ca5b47740");

    protected ActiveVanquisher(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_UUID.toString(), 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_UUID.toString(), 1, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(ALObjects.Attributes.LIFE_STEAL.get(), LIFE_STEAL_MODIFIER_UUID.toString(), 0.2, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierValue(int pAmplifier, AttributeModifier pModifier) {
        return pModifier.getAmount();
    }


}
