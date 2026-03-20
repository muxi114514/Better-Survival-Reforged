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

/**
 * Compatibility layer for Defiled Lands (1.20.1).
 * <p>
 * Provides Umbrium (iron-equivalent, applies Bleeding on hit) and
 * Scarlite (diamond-equivalent, heals attacker on hit) weapon tiers.
 */
public final class DefiledCompat {

    private DefiledCompat() {
    }

    // ── Cached references ──────────────────────────────────────────
    private static Tier UMBRIUM;
    private static Tier SCARLITE;
    private static MobEffect BLEEDING_EFFECT;

    private static boolean initialized = false;
    private static boolean bleedingResolved = false;

    /** Pairs of (Tier, lowercaseName) for weapon registration. */
    public record DefiledTierEntry(Tier tier, String name) {
    }

    // ══════════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════════

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        // Tiers — hardcoded to match Defiled Lands ModTiers values.
        // UMBRIUM: level=2, uses=600, speed=6.0, damage=2.0, enchant=14
        // SCARLITE: level=3, uses=1561, speed=8.0, damage=3.0, enchant=10
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
        // NOTE: Bleeding effect is resolved lazily on first hit, NOT here.
        // RegistrySupplier.get() would fail during static init.
    }

    /**
     * Returns Defiled Lands tier entries for weapon registration.
     */
    public static List<DefiledTierEntry> getDefiledTierEntries() {
        init();
        List<DefiledTierEntry> list = new ArrayList<>();
        if (UMBRIUM != null)
            list.add(new DefiledTierEntry(UMBRIUM, "umbrium"));
        if (SCARLITE != null)
            list.add(new DefiledTierEntry(SCARLITE, "scarlite"));
        return list;
    }

    // ══════════════════════════════════════════════════════════════════
    // On-hit effects
    // ══════════════════════════════════════════════════════════════════

    /**
     * Apply Defiled Lands on-hit effects.
     * Called from CommonEventHandler when the attacker uses a Defiled Lands weapon.
     *
     * @param stack    the weapon
     * @param target   entity being hurt
     * @param attacker the attacking player (may be null)
     * @param damage   actual damage dealt
     */
    public static void applyOnHitEffect(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, float damage) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return;

        Tier tier = weapon.getTier();

        // Debug: log all tier comparisons
        BetterSurvival.LOGGER.debug(
                "[DefiledCompat] applyOnHitEffect called. tier={}, UMBRIUM={}, SCARLITE={}, tierMatch_UMB={}, tierMatch_SCAR={}",
                tier, UMBRIUM, SCARLITE, tier == UMBRIUM, tier == SCARLITE);

        // Umbrium: apply Bleeding I for 80 ticks (4 seconds)
        if (tier == UMBRIUM) {
            resolveBleedingIfNeeded();
            BetterSurvival.LOGGER.info("[DefiledCompat] Umbrium hit! BLEEDING_EFFECT={}", BLEEDING_EFFECT);
            if (BLEEDING_EFFECT != null) {
                target.addEffect(new MobEffectInstance(BLEEDING_EFFECT, 80, 0));
                BetterSurvival.LOGGER.info("[DefiledCompat] Applied Bleeding to {}", target.getName().getString());
            }
        }

        // Scarlite: heal attacker based on damage dealt (min 1.0 HP = half heart)
        if (tier == SCARLITE && attacker != null) {
            float healAmount = Math.max(damage * 0.2F, 1.0F);
            attacker.heal(healAmount);
            BetterSurvival.LOGGER.info("[DefiledCompat] Scarlite healed {} for {} HP", attacker.getName().getString(),
                    healAmount);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Tooltip
    // ══════════════════════════════════════════════════════════════════

    /**
     * Add tooltip lines for Defiled Lands weapons.
     */
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

    // ══════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════

    public static boolean isDefiledTier(Tier tier) {
        return tier == UMBRIUM || tier == SCARLITE;
    }

    /**
     * Lazily resolve the Bleeding effect from Defiled Lands registry.
     * Called on first hit, NOT during init (RegistrySupplier.get() would fail).
     */
    private static void resolveBleedingIfNeeded() {
        if (bleedingResolved)
            return;
        bleedingResolved = true;
        try {
            // Use Forge registry lookup directly — no reflection needed
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
