package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class DefiledCompat {

    private DefiledCompat() {
    }

    private static Tier UMBRIUM;
    private static Tier SCARLITE;
    private static MobEffect BLEEDING_EFFECT;

    private static boolean initialized = false;
    private static boolean bleedingResolved = false;

    public record DefiledTierEntry(Tier tier, String name) {
    }

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        try {
            if (!net.minecraftforge.fml.ModList.get().isLoaded("defiledlands"))
                return;

            UMBRIUM = new net.minecraftforge.common.ForgeTier(
                    2, 600, 6.0F, 2.0F, 14,
                    net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            SCARLITE = new net.minecraftforge.common.ForgeTier(
                    3, 1561, 8.0F, 3.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            BetterSurvival.LOGGER.info("Defiled Lands tiers created successfully.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to create Defiled Lands tiers: {}", e.getMessage());
            UMBRIUM = SCARLITE = null;
        }

    }

    public static List<DefiledTierEntry> getDefiledTierEntries() {
        init();
        List<DefiledTierEntry> list = new ArrayList<>();
        if (UMBRIUM != null)
            list.add(new DefiledTierEntry(UMBRIUM, "umbrium"));
        if (SCARLITE != null)
            list.add(new DefiledTierEntry(SCARLITE, "scarlite"));
        return list;
    }

    public static void applyOnHitEffect(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, float damage) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return;

        Tier tier = weapon.getTier();

        BetterSurvival.LOGGER.debug(
                "[DefiledCompat] applyOnHitEffect called. tier={}, UMBRIUM={}, SCARLITE={}, tierMatch_UMB={}, tierMatch_SCAR={}",
                tier, UMBRIUM, SCARLITE, tier == UMBRIUM, tier == SCARLITE);

        if (tier == UMBRIUM) {
            resolveBleedingIfNeeded();
            BetterSurvival.LOGGER.info("[DefiledCompat] Umbrium hit! BLEEDING_EFFECT={}", BLEEDING_EFFECT);
            if (BLEEDING_EFFECT != null) {
                target.addEffect(new MobEffectInstance(BLEEDING_EFFECT, 80, 0));
                BetterSurvival.LOGGER.info("[DefiledCompat] Applied Bleeding to {}", target.getName().getString());
            }
        }

        if (tier == SCARLITE && attacker != null) {
            float healAmount = Math.max(damage * 0.2F, 1.0F);
            attacker.heal(healAmount);
            BetterSurvival.LOGGER.info("[DefiledCompat] Scarlite healed {} for {} HP", attacker.getName().getString(),
                    healAmount);
        }
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return;

        Tier tier = weapon.getTier();

        if (tier == UMBRIUM) {
            tooltip.add(Component.translatable("defiled_weapon_umbrium.hurt")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (tier == SCARLITE) {
            tooltip.add(Component.translatable("defiled_weapon_scarlite.hurt")
                    .withStyle(ChatFormatting.DARK_RED));
        }
    }

    public static boolean isDefiledTier(Tier tier) {
        return tier == UMBRIUM || tier == SCARLITE;
    }

    private static void resolveBleedingIfNeeded() {
        if (bleedingResolved)
            return;
        bleedingResolved = true;
        try {

            net.minecraft.resources.ResourceLocation bleedingId = new net.minecraft.resources.ResourceLocation(
                    "defiledlands", "bleeding");
            BLEEDING_EFFECT = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(bleedingId);
            if (BLEEDING_EFFECT != null) {
                BetterSurvival.LOGGER.info("Defiled Lands Bleeding effect resolved via registry: {}", bleedingId);
            } else {
                BetterSurvival.LOGGER.warn("Defiled Lands Bleeding effect NOT found in registry: {}", bleedingId);
            }
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to resolve Defiled Lands Bleeding effect: {}", e.getMessage());
            BLEEDING_EFFECT = null;
        }
    }
}
