package com.mx.bettersurvival.client;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.INunchakuCombo;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.init.ModEntities;
import com.mx.bettersurvival.init.ModItems;
import com.mx.bettersurvival.items.NunchakuItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

public class ClientEventHandler {

    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ResourceLocation spinning = new ResourceLocation(BetterSurvival.MOD_ID, "spinning");
                ResourceLocation blocking = new ResourceLocation(BetterSurvival.MOD_ID, "blocking");
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
                    } else if (reg.get() instanceof com.mx.bettersurvival.items.CustomShieldItem shield) {
                        // 举盾时切换到 _blocking 模型（与原版盾同理）
                        ItemProperties.register(shield, blocking,
                                (stack, level, entity, seed) -> entity != null && entity.isUsingItem()
                                        && entity.getUseItem() == stack ? 1.0F : 0.0F);
                    }
                }
            });
        }

        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.FLYING_SPEAR.get(), FlyingSpearRenderer::new);
        }
    }

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

            // Spin/attack logic lives in NunchakuClient (client-only class).
            NunchakuClient.tickSpin(mc, player);
        }
    }

    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, value = Dist.CLIENT)
    public static class ForgeTooltipEvents {
        @SubscribeEvent
        public static void onItemTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            // BS weapons are SwordItems now but already add this line in CustomWeaponItem.appendHoverText.
            if (stack.getItem() instanceof net.minecraft.world.item.SwordItem
                    && !(stack.getItem() instanceof com.mx.bettersurvival.items.CustomWeaponItem)) {
                com.mx.bettersurvival.items.CustomWeaponItem.addVenomTooltip(stack, event.getToolTip());
            }
        }
    }
}
