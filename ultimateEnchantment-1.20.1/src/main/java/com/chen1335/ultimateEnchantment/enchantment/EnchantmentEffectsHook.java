package com.chen1335.ultimateEnchantment.enchantment;

import net.minecraft.world.entity.LivingEntity;

public class EnchantmentEffectsHook {
    public static void onLivingHealthChange(LivingEntity living) {
        updateLastStandState(living);
    }

    public static void updateLastStandState(LivingEntity living) {
        living.getArmorSlots().forEach(itemStack -> {
            itemStack.getOrCreateTag().putFloat("ue:userHealth", living.getHealth());
            itemStack.getOrCreateTag().putFloat("ue:userMaxHealth", living.getMaxHealth());
        });
    }
}
