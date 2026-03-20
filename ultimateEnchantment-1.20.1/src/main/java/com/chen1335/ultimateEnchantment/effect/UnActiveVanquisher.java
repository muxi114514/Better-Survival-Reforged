package com.chen1335.ultimateEnchantment.effect;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class UnActiveVanquisher extends MobEffect {
    public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("cf8f57cb-315c-4df5-8125-7a1adca33552");
    public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("15d94692-f590-494e-aaca-d5d37bf5209a");

    protected UnActiveVanquisher(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_MODIFIER_UUID.toString(), 0.05, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, ATTACK_SPEED_MODIFIER_UUID.toString(), 0.1, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static void renderLevel(GuiGraphics pGuiGraphics, Font font, int x, int y, MobEffectInstance mobeffectinstance) {
        MutableComponent component = Component.translatable("enchantment.level." + (mobeffectinstance.getAmplifier() + 1));
        int width = font.width(component);
        pGuiGraphics.drawString(font, Component.translatable("enchantment.level." + (mobeffectinstance.getAmplifier() + 1)), x-width/2+10, y+10, 16777215);
    }
}
