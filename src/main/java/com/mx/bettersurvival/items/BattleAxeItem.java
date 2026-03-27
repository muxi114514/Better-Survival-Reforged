package com.mx.bettersurvival.items;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class BattleAxeItem extends CustomWeaponItem {

    public BattleAxeItem(Tier tier, float damageModifier, float speedModifier, Properties properties) {
        super(tier, damageModifier, speedModifier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(BetterSurvival.MOD_ID + ".battleaxe.desc")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {

        if (enchantment.category == EnchantmentCategory.DIGGER) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }
}
