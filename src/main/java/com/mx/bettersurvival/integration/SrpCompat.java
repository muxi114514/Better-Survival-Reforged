package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.init.ModTiers;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;
import java.util.List;

/**
 * SRP（Scape and Run: Parasites）风味材料的特效接缝——当前为预留接口。
 *
 * <p>感知(living)/觉知(sentient) 两套武器常驻注册、无配方（寄生虫暂无 1.20.1 版）。
 * 命中特效与 tooltip 在此统一预留；将来设计定稿或寄生虫出高版本后，只需填充本类，
 * 事件与物品侧的接线已就位（与 IaF/暮色/灾变等接缝同构）。
 */
public final class SrpCompat {

    private SrpCompat() {
    }

    public static boolean isLivingTier(Tier tier) {
        return tier == ModTiers.LIVING;
    }

    public static boolean isSentientTier(Tier tier) {
        return tier == ModTiers.SENTIENT;
    }

    /** 命中特效接口（预留）：当前无效果，返回附加伤害 0。 */
    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, boolean applyEffects) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return 0.0F;
        Tier tier = weapon.getTier();

        if (tier == ModTiers.LIVING) {
            // TODO: 感知命中特效（待设计）
        } else if (tier == ModTiers.SENTIENT) {
            // TODO: 觉知命中特效（待设计）
        }
        return 0.0F;
    }

    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker) {
        return getMaterialModifier(stack, target, attacker, true);
    }

    /** tooltip 接口（预留）：特效定稿后在此追加说明行。 */
    public static void addMaterialTooltip(Tier tier, List<Component> tooltip) {
    }
}
