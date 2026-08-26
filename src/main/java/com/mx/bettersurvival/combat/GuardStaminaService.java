package com.mx.bettersurvival.combat;

import com.mx.bettersurvival.capability.IGuardStamina;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.init.ModMobEffects;
import com.mx.bettersurvival.items.CustomShieldItem;
import com.mx.bettersurvival.network.GuardStaminaSyncPacket;
import com.mx.bettersurvival.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

/**
 * 格挡精力服务（单一职责：精力的计算与状态变更；事件处理器只负责调用）。
 *
 * <p>全部为服务端权威操作 + 同步；有效数值（消耗/稳定度/回复）在此按配置与盾牌附魔计算，
 * 客户端攻击闸门也复用 {@link #attackCost(ItemStack)} 保持一致。
 */
public final class GuardStaminaService {

    private GuardStaminaService() {
    }

    public static boolean enabled() {
        return ModConfig.COMMON.guardStaminaEnabled.get();
    }

    public static float max() {
        return ModConfig.COMMON.guardStaminaMax.get().floatValue();
    }

    /** 持盾攻击消耗（BlockPower 递减）。 */
    public static float attackCost(ItemStack shield) {
        float base = ModConfig.COMMON.guardStaminaAttackCost.get().floatValue();
        double red = ModConfig.COMMON.guardStaminaBlockPowerCostReduction.get();
        int bp = shield == null ? 0 : shield.getEnchantmentLevel(ModEnchantments.BLOCK_POWER.get());
        return (float) (base / (1.0 + bp * red));
    }

    /** 被击扣精力（稳定度：盾重 + BlockPower 递减）。 */
    private static float blockDrain(ItemStack shield, float blockedDamage) {
        float per = ModConfig.COMMON.guardStaminaBlockDrainPerDamage.get().floatValue();
        double stability = 1.0;
        if (shield.getItem() instanceof CustomShieldItem cs) {
            stability += cs.getWeight() * ModConfig.COMMON.guardStaminaWeightStability.get();
        }
        stability += shield.getEnchantmentLevel(ModEnchantments.BLOCK_POWER.get())
                * ModConfig.COMMON.guardStaminaBlockPowerCostReduction.get();
        return (float) (blockedDamage * per / stability);
    }

    /** 服务端每 tick：破防冷却递减、回复（举盾时减速）、按需同步。 */
    public static void serverTick(ServerPlayer player) {
        if (!enabled()) {
            return;
        }
        player.getCapability(ModCapabilities.GUARD_STAMINA).ifPresent(cap -> {
            boolean changed = false;
            float max = max();
            if (cap.getStamina() > max) {
                cap.setStamina(max);
                changed = true;
            }
            if (cap.getGuardCooldown() > 0) {
                cap.setGuardCooldown(cap.getGuardCooldown() - 1);
                changed = true;
            }
            if (cap.getRegenPause() > 0) {
                cap.setRegenPause(cap.getRegenPause() - 1);
            } else if (cap.getStamina() < max) {
                float perTick = ModConfig.COMMON.guardStaminaRegenPerTick.get().floatValue();
                float mult = player.isBlocking()
                        ? ModConfig.COMMON.guardStaminaRegenBlockingMult.get().floatValue()
                        : 1.0F;
                cap.setStamina(Math.min(max, cap.getStamina() + perTick * mult));
                changed = true;
            }
            if (changed) {
                sync(player, cap);
            }
        });
    }

    /** 尝试为一次持盾攻击扣精力；不足返回 false（由调用方否决攻击）。 */
    public static boolean tryConsumeAttack(ServerPlayer player, ItemStack shield) {
        if (!enabled()) {
            return true;
        }
        return player.getCapability(ModCapabilities.GUARD_STAMINA).map(cap -> {
            float cost = attackCost(shield);
            if (cap.getStamina() < cost) {
                return false;
            }
            cap.setStamina(cap.getStamina() - cost);
            cap.setRegenPause(ModConfig.COMMON.guardStaminaRegenDelay.get());
            sync(player, cap);
            return true;
        }).orElse(true);
    }

    /** 格挡吸收一次伤害：扣精力；打空则破防。 */
    public static void onBlocked(ServerPlayer player, ItemStack shield, float blockedDamage) {
        if (!enabled() || blockedDamage <= 0.0F) {
            return;
        }
        player.getCapability(ModCapabilities.GUARD_STAMINA).ifPresent(cap -> {
            cap.setRegenPause(ModConfig.COMMON.guardStaminaRegenDelay.get());
            float next = cap.getStamina() - blockDrain(shield, blockedDamage);
            if (next <= 0.0F) {
                cap.setStamina(0.0F);
                triggerBreak(player, cap);
            } else {
                cap.setStamina(next);
            }
            sync(player, cap);
        });
    }

    /** 破防：设冷却（期间禁止举盾）、强制落盾、施加眩晕、放破盾音效。 */
    private static void triggerBreak(ServerPlayer player, IGuardStamina cap) {
        cap.setGuardCooldown(ModConfig.COMMON.guardBreakCooldownTicks.get());
        player.stopUsingItem();
        int stun = ModConfig.COMMON.guardBreakStunTicks.get();
        if (stun > 0 && ModMobEffects.STUN.isPresent()) {
            player.addEffect(new MobEffectInstance(ModMobEffects.STUN.get(), stun));
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.SHIELD_BREAK,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /** 破防冷却是否生效（禁止重新举盾）。两端各读本地已同步的 capability。 */
    public static boolean isOnGuardCooldown(Player player) {
        return player.getCapability(ModCapabilities.GUARD_STAMINA)
                .map(cap -> cap.getGuardCooldown() > 0).orElse(false);
    }

    private static void sync(ServerPlayer player, IGuardStamina cap) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new GuardStaminaSyncPacket(cap.getStamina(), cap.getGuardCooldown()));
    }
}
