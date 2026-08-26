package com.mx.bettersurvival.client;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.IGuardStamina;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * 格挡精力的客户端侧：接收同步值写入本地 capability，并绘制 HUD 精力条。
 *
 * <p>经 {@code DistExecutor} 由同步包调用，客户端类不会被专用服务端加载。
 */
@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GuardStaminaClient {

    private GuardStaminaClient() {
    }

    /** 同步包回调：写入本人客户端 capability（供 HUD 与攻击闸门读取）。 */
    public static void apply(float stamina, int cooldown) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.getCapability(ModCapabilities.GUARD_STAMINA).ifPresent(cap -> {
                cap.setStamina(stamina);
                cap.setGuardCooldown(cooldown);
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "guard_stamina",
                (gui, graphics, partialTick, screenWidth, screenHeight) -> render(gui, graphics, screenWidth,
                        screenHeight));
    }

    private static void render(ForgeGui gui, GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = gui.getMinecraft();
        if (mc.options.hideGui) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        if (!ModConfig.CLIENT.attackWhileBlocking.get() || !ModConfig.CLIENT.guardStaminaHud.get()
                || !ModConfig.COMMON.guardStaminaEnabled.get()) {
            return;
        }

        Optional<IGuardStamina> opt = player.getCapability(ModCapabilities.GUARD_STAMINA).resolve();
        if (opt.isEmpty()) {
            return;
        }
        IGuardStamina cap = opt.get();
        float max = ModConfig.COMMON.guardStaminaMax.get().floatValue();
        float stamina = Math.min(cap.getStamina(), max);
        int cooldown = cap.getGuardCooldown();

        // 仅在相关时显示：持盾 / 举盾 / 破防冷却中 / 精力未满。
        boolean holdingShield = player.getMainHandItem().getUseAnimation() == UseAnim.BLOCK
                || player.getOffhandItem().getUseAnimation() == UseAnim.BLOCK;
        if (!holdingShield && !player.isBlocking() && cooldown <= 0 && stamina >= max - 0.5F) {
            return;
        }

        int barWidth = 101;
        int barHeight = 5;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight - 55;

        graphics.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0x90000000);
        graphics.fill(x, y, x + barWidth, y + barHeight, 0xFF3A3A3A);

        float frac = max <= 0.0F ? 0.0F : Mth.clamp(stamina / max, 0.0F, 1.0F);
        int fillWidth = (int) (barWidth * frac);
        int color = cooldown > 0 ? 0xFFFF5555 : (frac < 0.34F ? 0xFFFFC24B : 0xFF6FD66F);
        if (fillWidth > 0) {
            graphics.fill(x, y, x + fillWidth, y + barHeight, color);
        }
    }
}
