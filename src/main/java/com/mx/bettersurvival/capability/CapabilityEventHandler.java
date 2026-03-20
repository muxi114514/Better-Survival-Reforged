package com.mx.bettersurvival.capability;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles capability registration and attachment for BetterSurvival.
 *
 * RegisterCapabilitiesEvent → MOD bus
 * AttachCapabilitiesEvent → FORGE bus
 *
 * We split into two inner classes with different bus annotations.
 */
public class CapabilityEventHandler {

    private static final ResourceLocation ARROW_PROPS_KEY = new ResourceLocation(BetterSurvival.MOD_ID,
            "arrow_properties");
    private static final ResourceLocation NUNCHAKU_COMBO_KEY = new ResourceLocation(BetterSurvival.MOD_ID,
            "nunchaku_combo");
    private static final ResourceLocation SPEARS_IN_KEY = new ResourceLocation(BetterSurvival.MOD_ID,
            "spears_in");

    /**
     * MOD bus events — capability interface registration.
     */
    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {

        @SubscribeEvent
        public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
            event.register(IArrowProperties.class);
            event.register(INunchakuCombo.class);
            event.register(ISpearsIn.class);
        }
    }

    /**
     * FORGE bus events — capability attachment to entities.
     */
    @Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID)
    public static class ForgeBusEvents {

        @SubscribeEvent
        public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
            // Attach ArrowProperties to all arrows
            if (event.getObject() instanceof AbstractArrow) {
                event.addCapability(ARROW_PROPS_KEY, new ArrowPropertiesProvider());
            }

            // Attach NunchakuCombo to all players
            if (event.getObject() instanceof Player) {
                event.addCapability(NUNCHAKU_COMBO_KEY, new NunchakuComboProvider());
            }

            // Attach SpearsIn to all living entities (track stuck spears)
            if (event.getObject() instanceof LivingEntity) {
                event.addCapability(SPEARS_IN_KEY, new SpearsInProvider());
            }
        }
    }
}
