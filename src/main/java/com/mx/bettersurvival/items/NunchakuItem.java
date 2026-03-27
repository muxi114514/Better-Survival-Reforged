package com.mx.bettersurvival.items;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.init.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class NunchakuItem extends CustomWeaponItem {

    public NunchakuItem(Tier tier, float damageModifier, float speedModifier, Properties properties) {
        super(tier, damageModifier, speedModifier, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        int knockbackMod = EnchantmentHelper.getKnockbackBonus(attacker) + 1;
        target.knockback(-(float) knockbackMod * 0.1F,
                Mth.sin(attacker.getYRot() * knockbackMod * 0.017453292F),
                -Mth.cos(attacker.getYRot() * knockbackMod * 0.017453292F));

        if (attacker instanceof Player player) {
            player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                int comboLevel = EnchantmentHelper.getItemEnchantmentLevel(
                        ModEnchantments.COMBO.get(), stack);
                combo.setComboPower(combo.getComboPower() + 0.1F + comboLevel / 20F);
            });
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return player.getCapability(ModCapabilities.NUNCHAKU_COMBO)
                .map(combo -> {
                    if (!combo.isSpinning()) {
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, false);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(BetterSurvival.MOD_ID + ".nunchaku.desc")
                .withStyle(ChatFormatting.AQUA));
    }
}
