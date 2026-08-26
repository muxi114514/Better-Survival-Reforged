package com.mx.bettersurvival.mixin.client;

import com.mx.bettersurvival.capability.IGuardStamina;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.combat.GuardStaminaService;
import com.mx.bettersurvival.combat.ShieldAttackFilter;
import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 副手持盾防御时放行主手攻击。
 *
 * <p><b>真正的原版守卫在 {@code Minecraft.handleKeybinds()}</b>：当 {@code player.isUsingItem()}
 * 为真（举盾即为真）时，该方法走进一个分支，把本 tick 所有待处理的攻击点击
 * {@code while (keyAttack.consumeClick()) {}} <b>直接丢弃、根本不调用 {@code startAttack()}</b>。
 * 这就是"原版长按格挡时主手必定无法攻击"的根因——与 Better Combat 无关，改 {@code startAttack} 无效。
 *
 * <p><b>原理</b>：本 Mixin 在 {@code handleKeybinds} 的 HEAD 抢先执行——仅当"副手举盾 + 准星指向实体"时，
 * 把待处理的攻击点击取出并直接发起实体攻击（{@code gameMode.attack} + 主手挥动），全程不
 * {@code stopUsingItem}（举盾保持）。这些点击被消费后，原版那段"丢弃点击"已无内容可丢，故不重复处理；
 * 而原版分支的"松开右键 → releaseUsingItem 落盾"逻辑我们完全不碰，照常生效。
 *
 * <p>直接调用 {@code gameMode.attack} 而非 {@code startAttack()}，同时绕开 Better Combat 对
 * {@code doAttack} 的接管（其 upswing 在 {@code isUsingItem} 时同样放弃），故对原版武器与 BC 武器均生效。
 * 纯客户端输入层，伤害仍由服务端结算。
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void bettersurvival$attackWhileBlocking(CallbackInfo ci) {
        if (!ModConfig.CLIENT.attackWhileBlocking.get()) {
            return;
        }
        Minecraft mc = (Minecraft) (Object) this;
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null || mc.screen != null) {
            return;
        }
        // 仅"副手持盾防御"放行：isBlocking 涵盖任意盾牌（不限本模组），再要求使用手为副手。
        if (!player.isBlocking() || player.getUsedItemHand() != InteractionHand.OFF_HAND) {
            return;
        }
        // 主手武器黑/白名单过滤（空手由 allowEmptyHand 决定）。
        if (!ShieldAttackFilter.get().allows(player.getMainHandItem())) {
            return;
        }
        // 格挡精力闸门：精力不足以支付一次攻击时，不发起攻击（服务端还会再校验一次）。
        if (GuardStaminaService.enabled()) {
            float cost = GuardStaminaService.attackCost(player.getUseItem());
            float have = player.getCapability(ModCapabilities.GUARD_STAMINA)
                    .map(IGuardStamina::getStamina).orElse(0.0F);
            if (have < cost) {
                return;
            }
        }
        // 抢在原版"举盾丢弃点击"之前消费攻击点击；仅对准星实体发起攻击，
        // 挖掘/空挥保持与原版一致（点击被消费但不动作）。
        while (mc.options.keyAttack.consumeClick()) {
            HitResult hit = mc.hitResult;
            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                mc.gameMode.attack(player, ((EntityHitResult) hit).getEntity());
                player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }
}
