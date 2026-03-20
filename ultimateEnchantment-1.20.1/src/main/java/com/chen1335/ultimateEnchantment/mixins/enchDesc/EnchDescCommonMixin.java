package com.chen1335.ultimateEnchantment.mixins.enchDesc;

import net.darkhax.enchdesc.ConfigSchema;
import net.darkhax.enchdesc.DescriptionManager;
import net.darkhax.enchdesc.EnchDescCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(value = EnchDescCommon.class,remap = false)
public class EnchDescCommonMixin {
    @Shadow
    @Final
    private ConfigSchema config;

    @Shadow
    @Final
    private DescriptionManager descriptions;

    @Inject(method = "onItemTooltip", at = @At("HEAD"), cancellable = true)
    private void onItemTooltip(ItemStack stack, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        ci.cancel();
        if (this.config.enableMod && !stack.isEmpty() && stack.hasTag() && (!this.config.onlyDisplayOnBooks && stack.isEnchanted() || stack.getItem() instanceof EnchantedBookItem) && (!this.config.onlyDisplayInEnchantingTable || Minecraft.getInstance().screen instanceof EnchantmentScreen)) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            if (!enchantments.isEmpty()) {
                if (this.config.requireKeybindPress && !Screen.hasShiftDown()) {
                    tooltip.add(Component.translatable("enchdesc.activate.message").withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    Iterator<Enchantment> var5 = enchantments.keySet().iterator();

                    while (true) {
                        while (var5.hasNext()) {
                            Enchantment enchantment = var5.next();
                            Component fullName = enchantment.getFullname(enchantments.get(enchantment));
                            Iterator<Component> var8 = tooltip.iterator();

                            while (var8.hasNext()) {
                                Component line = var8.next();
                                if (line.getContents().equals(fullName.getContents())) {
                                    Component descriptionText = this.descriptions.get(enchantment);
                                    if (this.config.indentSize > 0) {
                                        descriptionText = Component.literal(StringUtils.repeat(' ', this.config.indentSize)).append(descriptionText);
                                    }

                                    tooltip.add(tooltip.indexOf(line) + 1, descriptionText);
                                    break;
                                }
                            }
                        }

                        return;
                    }
                }
            }
        }
    }
}
