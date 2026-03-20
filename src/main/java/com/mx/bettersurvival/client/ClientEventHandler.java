package com.mx.bettersurvival.client;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.INunchakuCombo;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.init.ModEntities;
import com.mx.bettersurvival.init.ModItems;
import com.mx.bettersurvival.items.NunchakuItem;
import com.mx.bettersurvival.network.ModNetwork;
import com.mx.bettersurvival.network.SpinningPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Client-side event handler — mirrors original ModClientHandler from 1.12.
 *
 * 1. Registers "spinning" item property for model switching
 * (FMLClientSetupEvent)
 * 2. Detects left-click hold → sends SpinningPacket to server
 * 3. Triggers attacks from client when attack cooldown is ready
 */
public class ClientEventHandler {

    // ======================== MOD bus: item property registration
    // ========================

    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ResourceLocation spinning = new ResourceLocation(BetterSurvival.MOD_ID, "spinning");
                for (RegistryObject<?> reg : ModItems.ITEMS.getEntries()) {
                    if (reg.get() instanceof NunchakuItem nunchaku) {
                        ItemProperties.register(nunchaku, spinning,
                                (stack, level, entity, seed) -> {
                                    if (entity != null && entity.getMainHandItem() == stack) {
                                        return entity.getCapability(ModCapabilities.NUNCHAKU_COMBO)
                                                .map(combo -> combo.isSpinning() ? 1.0F : 0.0F)
                                                .orElse(0.0F);
                                    }
                                    return 0.0F;
                                });
                    }
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.FLYING_SPEAR.get(), FlyingSpearRenderer::new);
        }
    }

    // ======================== FORGE bus: client tick for spinning + attacks
    // ========================

    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, value = Dist.CLIENT)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END)
                return;

            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null || mc.level == null)
                return;

            ItemStack mainHand = player.getMainHandItem();
            boolean holdingNunchaku = mainHand.getItem() instanceof NunchakuItem;
            boolean attackKeyDown = mc.options.keyAttack.isDown();

            player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                if (holdingNunchaku && attackKeyDown
                        && !player.isPassenger()
                        && player.getUseItem().isEmpty()) {

                    // Start spinning if not already — send packet to server
                    if (!combo.isSpinning()) {
                        combo.setSpinning(true);
                        ModNetwork.CHANNEL.sendToServer(new SpinningPacket(true));
                    }

                    // Trigger attack when cooldown allows (mirrors original ModClientHandler)
                    if (player.getAttackStrengthScale(0.5F) >= 1.0F) {
                        Entity target = mc.crosshairPickEntity;
                        if (target != null && target != player && target.isAlive()) {
                            mc.gameMode.attack(player, target);
                            player.resetAttackStrengthTicker();
                        }
                    }
                } else if (combo.isSpinning()) {
                    // Stop spinning — send packet to server
                    combo.setSpinning(false);
                    ModNetwork.CHANNEL.sendToServer(new SpinningPacket(false));
                }
            });
        }
    }

    // ======================== FORGE bus: item tooltip for vanilla swords
    // ========================

    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, value = Dist.CLIENT)
    public static class ForgeTooltipEvents {
        @SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof net.minecraft.world.item.SwordItem) {
                com.mx.bettersurvival.items.CustomWeaponItem.addVenomTooltip(stack, event.getToolTip());
            }
        }
    }
}
