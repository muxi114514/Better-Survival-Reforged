package com.mx.bettersurvival.items;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Dagger weapon – deals backstab bonus damage when attacking from behind.
 */
public class DaggerItem extends CustomWeaponItem {

    public DaggerItem(Tier tier, float damageModifier, float speedModifier, Properties properties) {
        super(tier, damageModifier, speedModifier, properties);
    }

    /**
     * Calculate backstab multiplier based on attacker and target facing direction.
     */
    public float getBackstabMultiplier(LivingEntity user, Entity target) {
        double attackerYaw = Math.toRadians(user.getYRot());
        double targetYaw = Math.toRadians(target.getYRot());
        if (Math.abs(Math.sin(attackerYaw) - Math.sin(targetYaw)) < 0.5D
                && Math.abs(Math.cos(attackerYaw) - Math.cos(targetYaw)) < 0.5D) {
            // TODO: Factor in Assassinate enchantment level once enchantments are ported
            return 2.0F;
        }
        return 1.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(BetterSurvival.MOD_ID + ".dagger.desc")
                .withStyle(ChatFormatting.AQUA));
    }
}
