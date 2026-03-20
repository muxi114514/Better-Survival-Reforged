package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Compatibility layer for Ice and Fire CE (1.20.1).
 * <p>
 * All IaF CE class references are done through reflection to keep IaF
 * as an optional dependency. If IaF is not loaded, all methods return
 * safe defaults (0 modifier, empty tier list, etc.).
 * <p>
 * Chain lightning for lightning dragonbone weapons is delegated to JMixin's
 * {@code ChainLightningUtils} (called directly — JMixin is a compile dep).
 */
public final class IaFCompat {

    private IaFCompat() {
    }

    // ── Cached Tier references (resolved via reflection at init) ──────
    private static Tier COPPER;
    private static Tier SILVER;
    private static Tier DRAGONBONE;
    private static Tier FIRE_DRAGONBONE;
    private static Tier ICE_DRAGONBONE;
    private static Tier LIGHTNING_DRAGONBONE;
    private static Tier FIRE_DRAGONSTEEL;
    private static Tier ICE_DRAGONSTEEL;
    private static Tier LIGHTNING_DRAGONSTEEL;

    private static boolean initialized = false;

    /** Pairs of (Tier, lowercaseName) for weapon registration. */
    public record IafTierEntry(Tier tier, String name) {
    }

    // ══════════════════════════════════════════════════════════════════
    // Initialization
    // ══════════════════════════════════════════════════════════════════

    /**
     * Try to resolve IaF CE tiers from the IafItems class via reflection.
     * Must be called after IaF has been loaded (i.e. during or after mod
     * construction, not in a static initializer).
     */
    public static void init() {
        if (initialized)
            return;
        initialized = true;
        try {
            // Replicate Ice and Fire tiers using ForgeTier to avoid Class.forName()
            // deadlocks during parallel mod construction.
            COPPER = new net.minecraftforge.common.ForgeTier(2, 300, 0.7F, 0.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.of(net.minecraft.world.item.Items.COPPER_INGOT));

            SILVER = new net.minecraftforge.common.ForgeTier(2, 460, 11.0F, 1.0F, 18,
                    net.minecraft.tags.BlockTags.NEEDS_IRON_TOOL,
                    () -> {
                        net.minecraft.world.item.Item ing = net.minecraftforge.registries.ForgeRegistries.ITEMS
                                .getValue(new net.minecraft.resources.ResourceLocation("iceandfire", "silver_ingot"));
                        return ing != null ? net.minecraft.world.item.crafting.Ingredient.of(ing)
                                : net.minecraft.world.item.crafting.Ingredient.EMPTY;
                    });

            DRAGONBONE = new net.minecraftforge.common.ForgeTier(3, 1660, 10.0F, 4.0F, 22,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> {
                        net.minecraft.world.item.Item ing = net.minecraftforge.registries.ForgeRegistries.ITEMS
                                .getValue(new net.minecraft.resources.ResourceLocation("iceandfire", "dragonbone"));
                        return ing != null ? net.minecraft.world.item.crafting.Ingredient.of(ing)
                                : net.minecraft.world.item.crafting.Ingredient.EMPTY;
                    });

            FIRE_DRAGONBONE = new net.minecraftforge.common.ForgeTier(3, 2000, 10.0F, 5.5F, 22,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            ICE_DRAGONBONE = new net.minecraftforge.common.ForgeTier(3, 2000, 10.0F, 5.5F, 22,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            LIGHTNING_DRAGONBONE = new net.minecraftforge.common.ForgeTier(3, 2000, 10.0F, 5.5F, 22,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);

            BetterSurvival.LOGGER.info("IaF CE tiers resolved successfully via ForgeTier proxy.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to resolve IaF CE tiers: {}", e.getMessage());
            COPPER = SILVER = DRAGONBONE = FIRE_DRAGONBONE = ICE_DRAGONBONE = LIGHTNING_DRAGONBONE = null;
        }
        // Dragon Steel tiers — hardcoded to match IaF CE defaults.
        // We do NOT resolve repair ingredients here because RegistrySupplier.get()
        // will throw at this point (IaF items not yet in Forge registry).
        // Repair ingredient is only for anvil, so Ingredient.EMPTY is fine.
        // Values: harvestLevel=4, durability=4000, speed=4.0, damage=10.0,
        // enchantability=10
        try {
            // IaF presence is already asserted when `init()` is called, no need for
            // dangerous Class.forName() checks.
            FIRE_DRAGONSTEEL = new net.minecraftforge.common.ForgeTier(
                    4, 4000, 4.0F, 10.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            ICE_DRAGONSTEEL = new net.minecraftforge.common.ForgeTier(
                    4, 4000, 4.0F, 10.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            LIGHTNING_DRAGONSTEEL = new net.minecraftforge.common.ForgeTier(
                    4, 4000, 4.0F, 10.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);
            BetterSurvival.LOGGER.info("IaF CE Dragon Steel tiers created successfully.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to create IaF CE Dragon Steel tiers: {}", e.getMessage());
            FIRE_DRAGONSTEEL = ICE_DRAGONSTEEL = LIGHTNING_DRAGONSTEEL = null;
        }
    }

    /**
     * Returns IaF tier entries for weapon registration.
     * Called from {@code ModItems} static block.
     */
    public static List<IafTierEntry> getIafTierEntries() {
        init();
        List<IafTierEntry> list = new ArrayList<>();
        if (COPPER != null)
            list.add(new IafTierEntry(COPPER, "copper"));
        if (SILVER != null)
            list.add(new IafTierEntry(SILVER, "silver"));
        if (DRAGONBONE != null)
            list.add(new IafTierEntry(DRAGONBONE, "dragonbone"));
        if (FIRE_DRAGONBONE != null)
            list.add(new IafTierEntry(FIRE_DRAGONBONE, "firedragonbone"));
        if (ICE_DRAGONBONE != null)
            list.add(new IafTierEntry(ICE_DRAGONBONE, "icedragonbone"));
        if (LIGHTNING_DRAGONBONE != null)
            list.add(new IafTierEntry(LIGHTNING_DRAGONBONE, "lightningdragonbone"));
        if (FIRE_DRAGONSTEEL != null)
            list.add(new IafTierEntry(FIRE_DRAGONSTEEL, "firedragonsteel"));
        if (ICE_DRAGONSTEEL != null)
            list.add(new IafTierEntry(ICE_DRAGONSTEEL, "icedragonsteel"));
        if (LIGHTNING_DRAGONSTEEL != null)
            list.add(new IafTierEntry(LIGHTNING_DRAGONSTEEL, "lightningdragonsteel"));
        return list;
    }

    // ══════════════════════════════════════════════════════════════════
    // Material Modifier (damage bonus + effects)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Compute IaF material bonus damage and apply on-hit effects.
     *
     * @param stack        the weapon ItemStack
     * @param target       the entity being hit
     * @param attacker     the attacking player (may be null for spears)
     * @param applyEffects whether to apply on-hit effects (false for thrown spears)
     * @return bonus damage to add
     */
    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, boolean applyEffects) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return 0.0F;
        Tier tier = weapon.getTier();

        if (tier == SILVER) {
            if (target.getMobType() == MobType.UNDEAD)
                return 2.0F;
        } else if (tier == FIRE_DRAGONBONE) {
            if (applyEffects) {
                target.setSecondsOnFire(5);
            }
            if (isIceDragon(target))
                return 8.0F;
        } else if (tier == ICE_DRAGONBONE) {
            if (applyEffects) {
                applyFreezeEffect(target, 200);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
            }
            if (isFireDragon(target))
                return 8.0F;
        } else if (tier == LIGHTNING_DRAGONBONE) {
            if (applyEffects && attacker != null) {
                triggerChainLightning(target, attacker);
            }
            if (isFireDragon(target) || isIceDragon(target))
                return 4.0F;
        } else if (tier == FIRE_DRAGONSTEEL) {
            if (applyEffects) {
                target.setSecondsOnFire(8);
            }
            if (isIceDragon(target))
                return 12.0F;
        } else if (tier == ICE_DRAGONSTEEL) {
            if (applyEffects) {
                applyFreezeEffect(target, 300);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 3));
            }
            if (isFireDragon(target))
                return 12.0F;
        } else if (tier == LIGHTNING_DRAGONSTEEL) {
            if (applyEffects && attacker != null) {
                triggerChainLightning(target, attacker);
            }
            if (isFireDragon(target) || isIceDragon(target))
                return 6.0F;
        }
        // DRAGONBONE (plain) has no special effect
        return 0.0F;
    }

    /** Convenience overload — applies effects by default. */
    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker) {
        return getMaterialModifier(stack, target, attacker, true);
    }

    // ══════════════════════════════════════════════════════════════════
    // Effects
    // ══════════════════════════════════════════════════════════════════

    /**
     * Apply IaF CE's freeze effect via reflection.
     * Calls: IafEntityData.get(target).frozenData.setFrozen(target, duration)
     */
    private static void applyFreezeEffect(LivingEntity target, int duration) {
        try {
            Class<?> dataClass = Class.forName("com.iafenvoy.iceandfire.data.component.IafEntityData");
            Object data = dataClass.getMethod("get", LivingEntity.class).invoke(null, target);
            Object frozenData = data.getClass().getField("frozenData").get(data);
            frozenData.getClass()
                    .getMethod("setFrozen", LivingEntity.class, int.class)
                    .invoke(frozenData, target, duration);
        } catch (Exception e) {
            // Fallback: just apply slowness
            BetterSurvival.LOGGER.debug("Failed to apply IaF freeze: {}", e.getMessage());
        }
    }

    private static void triggerChainLightning(LivingEntity target, Entity attacker) {
        if (!BetterSurvival.isJMixinLoaded) {
            // Fallback: Summon a vanilla lightning bolt if JMixin is not installed
            if (!target.level().isClientSide
                    && target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT
                        .create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(target.position());
                    if (attacker instanceof net.minecraft.server.level.ServerPlayer sp) {
                        bolt.setCause(sp);
                    }
                    // Add IaF CE's standard tag to prevent loot destruction if its event handler
                    // catches it
                    bolt.addTag("IceAndFire_DontDestroyLoot");
                    serverLevel.addFreshEntity(bolt);
                }
            }
            return;
        }

        if (target.level().isClientSide)
            return;
        try {
            com.mx.jmixin.lightning.ChainLightningUtils.createChainLightning(
                    target.level(), target, attacker);
        } catch (Exception e) {
            BetterSurvival.LOGGER.debug("Chain lightning failed: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // Entity type checks
    // ══════════════════════════════════════════════════════════════════

    public static boolean isFireDragon(Entity entity) {
        return entity.getClass().getName().contains("EntityFireDragon");
    }

    public static boolean isIceDragon(Entity entity) {
        return entity.getClass().getName().contains("EntityIceDragon");
    }

    // ══════════════════════════════════════════════════════════════════
    // Tooltips
    // ══════════════════════════════════════════════════════════════════

    /**
     * Add material-specific tooltip lines to a BS weapon.
     */
    public static void addMaterialTooltip(Tier tier, List<Component> tooltip) {
        if (tier == SILVER) {
            tooltip.add(Component.translatable("silvertools.hurt")
                    .withStyle(ChatFormatting.GREEN));
        } else if (tier == DRAGONBONE) {
            // Plain dragonbone has no tooltip
        } else if (tier == FIRE_DRAGONBONE) {
            tooltip.add(Component.translatable("dragon_sword_fire.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_fire.hurt2")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (tier == ICE_DRAGONBONE) {
            tooltip.add(Component.translatable("dragon_sword_ice.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_ice.hurt2")
                    .withStyle(ChatFormatting.AQUA));
        } else if (tier == LIGHTNING_DRAGONBONE) {
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (tier == FIRE_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_fire.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_fire.hurt2")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (tier == ICE_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_ice.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_ice.hurt2")
                    .withStyle(ChatFormatting.AQUA));
        } else if (tier == LIGHTNING_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
