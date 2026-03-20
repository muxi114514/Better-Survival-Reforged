package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.enchantment.EnchantmentUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
//    @Inject(method = "setEnchantments", at = @At("HEAD"), cancellable = true)
//    private static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemStack, CallbackInfo ci) {
//        map = new LinkedHashMap<>(map);
//        EnchantmentUtils.sortInMap(map);
//        ListTag listtag = new ListTag();
//        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
//            Enchantment enchantment = entry.getKey();
//            if (enchantment != null) {
//                int i = entry.getValue();
//                listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment), i));
//                if (itemStack.is(Items.ENCHANTED_BOOK)) {
//                    EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, i));
//                }
//            }
//        }
//
//        if (listtag.isEmpty()) {
//            itemStack.removeTagKey("Enchantments");
//        } else if (!itemStack.is(Items.ENCHANTED_BOOK)) {
//            itemStack.addTagElement("Enchantments", listtag);
//        }
//        ci.cancel();
//    }
}
