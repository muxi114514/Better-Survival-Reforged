package com.chen1335.ultimateEnchantment.enchantment;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;


public interface IAttributeEnchantment {
    Multimap<Attribute, AttributeModifier> getAttributeModifier(EquipmentSlot slot, int level);

}
