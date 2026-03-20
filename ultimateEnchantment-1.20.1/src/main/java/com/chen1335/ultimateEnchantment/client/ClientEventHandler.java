package com.chen1335.ultimateEnchantment.client;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class ClientEventHandler {
    @Mod.EventBusSubscriber(modid = UltimateEnchantment.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
    public static class GameHandler {
        @SubscribeEvent
        public static void toolTip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(stack);
            if (stack.getItem() == Items.ENCHANTED_BOOK && enchMap.size() == 1) {
                Enchantment ench = enchMap.keySet().iterator().next();
                if (!ModList.get().isLoaded("enchdesc") && "ultimate_enchantment".equals(ForgeRegistries.ENCHANTMENTS.getKey(ench).getNamespace())) {
                    event.getToolTip().add(Component.translatable(ench.getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }
}
