package com.chen1335.ultimateEnchantment.common;

import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAttributeExtension;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

public class AttributeTypeInfo {
    public enum Sentiment {
        POSITIVE,
        NEUTRAL,
        NEGATIVE
    }

    public static void init() {
        ForgeRegistries.ATTRIBUTES.forEach(attribute -> {
            IAttributeExtension iAttributeExtension = (IAttributeExtension) attribute;
            if (attribute == ForgeMod.ENTITY_GRAVITY.get()) {
                iAttributeExtension.ue$SetSentiment(Sentiment.NEUTRAL);
            } else if (attribute == ForgeMod.NAMETAG_DISTANCE.get()) {
                iAttributeExtension.ue$SetSentiment(Sentiment.NEUTRAL);
            }
        });
    }
}
