package com.mx.bettersurvival.events;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.enchantments.*;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.init.ModMobEffects;
import com.mx.bettersurvival.items.BattleAxeItem;
import com.mx.bettersurvival.items.DaggerItem;
import com.mx.bettersurvival.items.HammerItem;
import com.mx.bettersurvival.items.NunchakuItem;
import com.mx.bettersurvival.items.CustomWeaponItem;
import com.mx.bettersurvival.items.SpearItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Central event handler that activates all BetterSurvival enchantment effects.
 * Auto-registered via @Mod.EventBusSubscriber.
 */
@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID)
public class CommonEventHandler {

    // Guard flag to prevent recursive Tunneling calls
    private static boolean isMiningTunneling = false;

    // ======================== Armor (tick-based) ========================

    /**
     * Agility → movement speed modifier (server-side only, modifies attribute)
     * Vitality → passive healing at full hunger (server-side only)
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide)
            return;

        // Agility — check legs slot directly for the enchantment
        ItemStack legsArmor = entity.getItemBySlot(EquipmentSlot.LEGS);
        int agilityLevel = legsArmor.getEnchantmentLevel(ModEnchantments.AGILITY.get());
        if (agilityLevel > 0) {
            AgilityEnchantment.applySpeedModifier(entity, agilityLevel);
        } else {
            AgilityEnchantment.removeSpeedModifier(entity);
        }

        // Vitality — check chest slot directly
        if (entity instanceof Player player) {
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
            int vitalityLevel = chestArmor.getEnchantmentLevel(ModEnchantments.VITALITY.get());
            VitalityEnchantment.healPlayer(player, vitalityLevel);

            // NunchakuCombo countdown (matches original: decrement comboTime each tick)
            if (player.getMainHandItem().getItem() instanceof NunchakuItem) {
                player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                    if (combo.getComboTime() > 0 || combo.getComboPower() > 0) {
                        combo.countDown();
                    }
                });
            }
        }

        // Stun — zero motion and defuse creepers
        if (entity.hasEffect(ModMobEffects.STUN.get())) {
            double y = entity.getDeltaMovement().y;
            entity.setDeltaMovement(0, y <= 0 ? y : 0, 0);
            if (entity instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
            }
        }

        // Blindness — reduce mob follow range
        if (entity instanceof Mob mob) {
            UUID blindUUID = UUID.fromString("a6107045-134f-4c14-a645-75c3ae5c7a27");
            var followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (followRange != null) {
                followRange.removeModifier(blindUUID);
                if (mob.hasEffect(MobEffects.BLINDNESS)) {
                    followRange.addTransientModifier(new AttributeModifier(
                            blindUUID, "blind", -0.75,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }
        }
    }

    // ======================== HighJump ========================

    /**
     * Boost Y velocity on jump.
     * IMPORTANT: runs on BOTH client and server — jump velocity is
     * client-authoritative,
     * the client must apply the boost for it to be visible.
     */
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        // Check feet slot directly for HighJump
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        int level = boots.getEnchantmentLevel(ModEnchantments.HIGH_JUMP.get());
        if (level > 0) {
            HighJumpEnchantment.boostJump(entity, level);
        }

        // Stun — prevent jumping
        if (entity.hasEffect(ModMobEffects.STUN.get())) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));
        }
    }

    // ======================== Education ========================

    /**
     * Multiply experience drops when killer has Education enchantment.
     */
    @SubscribeEvent
    public static void onExpDrop(LivingExperienceDropEvent event) {
        Player attacker = event.getAttackingPlayer();
        if (attacker == null)
            return;

        float multiplier = EducationEnchantment.getExpMultiplier(attacker, event.getEntity());
        if (multiplier > 1.0F) {
            event.setDroppedExperience((int) (event.getDroppedExperience() * multiplier));
        }
    }

    // ======================== Weapon Specials ========================

    /**
     * Assassinate → backstab damage multiplier (Dagger)
     * Bash → stun on full-strength hit (Hammer)
     * Disarm → force target to drop weapon (BattleAxe)
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player attacker))
            return;
        if (attacker.level().isClientSide)
            return;

        LivingEntity target = event.getEntity();
        ItemStack weapon = attacker.getMainHandItem();

        // ---- Assassinate ----
        if (weapon.getItem() instanceof DaggerItem dagger) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.ASSASSINATE.get());
            if (level > 0) {
                float backstabMult = dagger.getBackstabMultiplier(attacker, target);
                if (backstabMult > 1.0F) {
                    // Base backstab ×2, plus 0.5 per enchantment level
                    event.setAmount(event.getAmount() * (backstabMult + level * 0.5F));
                }
            }
        }

        // ---- Bash ----
        if (weapon.getItem() instanceof HammerItem) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.BASH.get());
            double stunChance = ModConfig.COMMON.stunBaseChance.get()
                    + level * ModConfig.COMMON.bashModifier.get();
            if (attacker.getAttackStrengthScale(0.5F) > 0.9F
                    && attacker.getRandom().nextFloat() < stunChance
                    && ModMobEffects.STUN.isPresent()) {
                target.addEffect(new MobEffectInstance(
                        ModMobEffects.STUN.get(), HammerItem.STUN_DURATION));
            }
        }

        // ---- Disarm ----
        if (weapon.getItem() instanceof BattleAxeItem) {
            int level = weapon.getEnchantmentLevel(ModEnchantments.DISARM.get());
            if (level > 0 && attacker.getAttackStrengthScale(0.5F) > 0.9F) {
                double disarmChance = ModConfig.COMMON.disarmBaseChance.get()
                        + level * ModConfig.COMMON.disarmModifier.get();
                if (attacker.getRandom().nextFloat() < disarmChance) {
                    ItemStack targetWeapon = target.getMainHandItem();
                    if (!targetWeapon.isEmpty()) {
                        target.spawnAtLocation(targetWeapon.copy());
                        target.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }
                }
            }
        }

        // ---- Combo (Nunchaku — damage = base × (comboPower + 1.0)) ----
        if (weapon.getItem() instanceof NunchakuItem) {
            attacker.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                event.setAmount(event.getAmount() * (combo.getComboPower() + 1.0F));
            });
        }

        // ---- IaF CE Material Modifier ----
        if (BetterSurvival.isIafLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            float bonus = com.mx.bettersurvival.integration.IaFCompat.getMaterialModifier(weapon, target, attacker);
            if (bonus > 0.0F) {
                event.setAmount(event.getAmount() + bonus);
            }
        }

        // ---- Crying Obsidian — Weakness on hit ----
        if (weapon.getItem() instanceof CustomWeaponItem cw
                && cw.getTier() == com.mx.bettersurvival.init.ModTiers.CRYING_OBSIDIAN) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0)); // Weakness I, 3s
        }

        // ---- Defiled Lands — Bleeding / Vampirism ----
        if (BetterSurvival.isDefiledLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            com.mx.bettersurvival.integration.DefiledCompat.applyOnHitEffect(
                    weapon, target, attacker, event.getAmount());
        }
    }

    // ======================== RapidFire ========================

    /**
     * Speeds up bow charge by reducing remaining use duration.
     * Runs on BOTH client and server for smooth animation sync.
     */
    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        ItemStack item = event.getItem();
        if (!(item.getItem() instanceof BowItem))
            return;

        // Check the bow item directly for RapidFire enchantment
        int rapidFireLevel = item.getEnchantmentLevel(ModEnchantments.RAPID_FIRE.get());
        if (rapidFireLevel <= 0)
            return;

        int reduction = RapidFireEnchantment.getChargeTimeReduction(
                entity, event.getDuration());
        if (reduction > 0) {
            event.setDuration(event.getDuration() - reduction);
        }
    }

    // ======================== Multishot ========================

    /**
     * Shoot extra arrows in a fan pattern when releasing the bow.
     */
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return;

        ItemStack bow = event.getBow();
        Level level = player.level();

        float power = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1F)
            return;

        int multishotLevel = bow.getEnchantmentLevel(ModEnchantments.MULTISHOT.get());
        if (multishotLevel > 0) {
            int extraArrows = multishotLevel * 2;
            for (int i = 0; i < extraArrows; i++) {
                // Alternate left/right: +10, -10, +20, -20, ...
                float angleOffset = ((i / 2) + 1) * 10.0F * (i % 2 == 0 ? 1 : -1);

                Arrow arrow = new Arrow(level, player);
                arrow.shootFromRotation(player, player.getXRot(),
                        player.getYRot() + angleOffset, 0.0F,
                        power * 3.0F, 1.0F);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                // Carry over Power enchantment
                int powerLevel = bow.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(
                            arrow.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
                }

                // Carry over Flame enchantment
                if (bow.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                    arrow.setSecondsOnFire(100);
                }

                // Critical if fully charged
                arrow.setCritArrow(power >= 1.0F);

                level.addFreshEntity(arrow);
            }
        }
    }

    // ======================== Range + ArrowRecovery stamp ========================

    /**
     * When an arrow joins the world:
     * 1. Boost arrow velocity if the shooter's bow has the Range enchantment.
     * 2. Stamp ArrowRecovery enchantment level into the arrow's Capability.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide)
            return;
        if (!(event.getEntity() instanceof AbstractArrow arrow))
            return;
        if (!(arrow.getOwner() instanceof Player player))
            return;

        // Check both hands for a bow
        ItemStack bow = findBow(player);
        if (bow.isEmpty())
            return;

        // Range — boost velocity
        int rangeLevel = bow.getEnchantmentLevel(ModEnchantments.RANGE.get());
        if (rangeLevel > 0) {
            double multiplier = RangeEnchantment.getVelocityMultiplier();
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(multiplier));
        }

        // ArrowRecovery — stamp level into arrow Capability
        int recoveryLevel = bow.getEnchantmentLevel(ModEnchantments.ARROW_RECOVERY.get());
        if (recoveryLevel > 0) {
            arrow.getCapability(ModCapabilities.ARROW_PROPERTIES).ifPresent(props -> {
                props.setRecoveryLevel(recoveryLevel);
            });
        }
    }

    // ======================== ArrowRecovery (Capability-based)
    // ========================

    /**
     * Arrow recovery using Capability: when an arrow hits an entity,
     * read the stored recovery level from the arrow's Capability and
     * recover the arrow directly to the player's inventory.
     */
    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide)
            return;
        if (!(event.getEntity() instanceof AbstractArrow arrow))
            return;
        if (!(arrow.getOwner() instanceof Player player))
            return;
        if (event.getRayTraceResult().getType() != HitResult.Type.ENTITY)
            return;

        // Read recovery level from the arrow's own Capability
        arrow.getCapability(ModCapabilities.ARROW_PROPERTIES).ifPresent(props -> {
            int level = props.getRecoveryLevel();
            if (level > 0) {
                float chance = Math.min(1.0F, level * 0.33F);
                if (player.getRandom().nextFloat() < chance) {
                    // Add arrow directly to player inventory
                    player.getInventory().add(new ItemStack(Items.ARROW));
                    arrow.discard();
                }
            }
        });
    }

    // ======================== Tunneling + Diamonds ========================

    /**
     * Tunneling → mine surrounding blocks in a plane.
     * Diamonds → chance to drop a diamond from stone-type blocks.
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide)
            return;

        BlockState state = event.getState();
        ItemStack tool = player.getMainHandItem();

        // Tunneling — mine extra blocks (guarded against recursion)
        if (!isMiningTunneling) {
            int tunnelingLevel = tool.getEnchantmentLevel(ModEnchantments.TUNNELING.get());
            if (tunnelingLevel > 0) {
                isMiningTunneling = true;
                try {
                    TunnelingEnchantment.mineManyBlocks(player, state, event.getPos());
                } finally {
                    isMiningTunneling = false;
                }
            }
        }

        // Diamonds — chance to drop a diamond when mining stone
        int diamondsLevel = tool.getEnchantmentLevel(ModEnchantments.DIAMONDS.get());
        if (diamondsLevel > 0 && isStoneBlock(state)) {
            float chance = diamondsLevel * 0.01F;
            if (player.getRandom().nextFloat() < chance) {
                Level level = player.level();
                ItemEntity diamond = new ItemEntity(level,
                        event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5,
                        event.getPos().getZ() + 0.5,
                        new ItemStack(Items.DIAMOND));
                level.addFreshEntity(diamond);
            }
        }
    }

    // ======================== Versatility ========================

    /**
     * Give a mining speed boost when the tool is not naturally effective.
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        int level = tool.getEnchantmentLevel(ModEnchantments.VERSATILITY.get());
        if (level > 0) {
            float modifier = VersatilityEnchantment.getSpeedModifier(
                    player, event.getState());
            if (modifier > 1.0F) {
                event.setNewSpeed(event.getOriginalSpeed() + modifier);
            }
        }
    }

    // ======================== Helpers ========================

    /**
     * Find a bow in the player's main hand or offhand.
     */
    private static ItemStack findBow(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof BowItem)
            return mainHand;
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof BowItem)
            return offHand;
        return ItemStack.EMPTY;
    }

    /**
     * Check if a block is "stone" (overworld or nether base stone).
     */
    private static boolean isStoneBlock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER);
    }

    // ======================== Spear Death Drop ========================

    /**
     * Drop all spears stuck in an entity when it dies.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide)
            return;

        entity.getCapability(ModCapabilities.SPEARS_IN).ifPresent(cap -> {
            for (ItemStack spear : cap.getSpearsIn()) {
                if (!spear.isEmpty()) {
                    entity.spawnAtLocation(spear, 0.1F);
                }
            }
            cap.clearSpears();
        });
    }

    // ======================== Spear Melee Break ========================

    /**
     * When a player hits with a spear, there is a chance it breaks (shrinks by 1).
     * Matches original LivingAttackEvent logic.
     */
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof Player player))
            return;
        if (player.level().isClientSide)
            return;

        ItemStack weapon = player.getMainHandItem();

        // Spear break chance
        if (weapon.getItem() instanceof SpearItem spear) {
            if (!player.getAbilities().instabuild
                    && spear.breakChance() >= player.getRandom().nextFloat()) {
                weapon.shrink(1);
            }
        }

        // Weapon venom: apply stored potion effects on hit
        if (weapon.hasTag() && weapon.getTag().contains("remainingPotionHits")) {
            int hits = weapon.getTag().getInt("remainingPotionHits");
            if (hits > 0) {
                LivingEntity target = event.getEntity();
                net.minecraft.world.item.alchemy.Potion potion = net.minecraft.world.item.alchemy.PotionUtils
                        .getPotion(weapon);
                double div = Math.max(1.0, com.mx.bettersurvival.config.ModConfig.COMMON.potionDurationDivisor.get());
                int ampMod = com.mx.bettersurvival.config.ModConfig.COMMON.potionAmplifierModifier.get();
                for (net.minecraft.world.effect.MobEffectInstance effect : potion.getEffects()) {
                    int newDuration = Math.max(1, (int) (effect.getDuration() / div));
                    int newAmp = Math.max(0, effect.getAmplifier() + ampMod);
                    target.addEffect(
                            new net.minecraft.world.effect.MobEffectInstance(effect.getEffect(), newDuration, newAmp));
                }
                weapon.getTag().putInt("remainingPotionHits", hits - 1);
                if (hits - 1 <= 0) {
                    weapon.removeTagKey("Potion");
                    weapon.removeTagKey("CustomPotionEffects");
                    weapon.removeTagKey("remainingPotionHits");
                }
            }
        }
    }

    // ======================== Antiwarp ========================

    /**
     * Prevent Enderman teleportation when the entity has the Antiwarp effect.
     */
    @SubscribeEvent
    public static void onEnderTeleport(net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event) {
        if (event.getEntityLiving().hasEffect(ModMobEffects.ANTIWARP.get())) {
            event.setCanceled(true);
        }
    }
}
