package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IEnchantmentExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Enchantment.class)
public class EnchantmentMixin implements IEnchantmentExtension {
    @Unique
    private UltimateEnchantment.EnchantmentType ue$enchantmentType = UltimateEnchantment.EnchantmentType.OTHER;

    @Unique
    public void ue$setEnchantmentType(UltimateEnchantment.EnchantmentType enchantmentType) {
        this.ue$enchantmentType = enchantmentType;
    }

    @Unique
    public UltimateEnchantment.EnchantmentType ue$getEnchantmentType() {
        return ue$enchantmentType;
    }

    @Inject(method = "getFullname", at = @At("RETURN"), cancellable = true)
    private void getFullname(int pLevel, CallbackInfoReturnable<Component> cir) {
        if (ue$enchantmentType == UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT) {
            cir.setReturnValue(cir.getReturnValue().copy().setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
        } else if (ue$enchantmentType == UltimateEnchantment.EnchantmentType.LEGENDARY_ENCHANTMENT) {
            cir.setReturnValue(cir.getReturnValue().copy().setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        }
    }
}
