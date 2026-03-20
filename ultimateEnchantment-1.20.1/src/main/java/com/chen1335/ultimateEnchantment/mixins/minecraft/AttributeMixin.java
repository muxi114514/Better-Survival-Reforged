package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.common.AttributeTypeInfo;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAttributeExtension;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Attribute.class)
public class AttributeMixin implements IAttributeExtension {
    @Unique
    private AttributeTypeInfo.Sentiment ue$sentiment = AttributeTypeInfo.Sentiment.POSITIVE;

    @Unique
    public void ue$SetSentiment(AttributeTypeInfo.Sentiment ue$sentiment) {
        this.ue$sentiment = ue$sentiment;
    }

    @Unique
    public AttributeTypeInfo.Sentiment ue$getSentiment() {
        return ue$sentiment;
    }
}
