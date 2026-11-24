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
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import com.mx.bettersurvival.mixin.AbstractArrowInvoker;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID)
public class CommonEventHandler {

    private static boolean isMiningTunneling = false;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide)
            return;

        ItemStack legsArmor = entity.getItemBySlot(EquipmentSlot.LEGS);
        int agilityLevel = ModEnchantments.level(ModEnchantments.AGILITY, legsArmor);
        if (agilityLevel > 0) {
            AgilityEnchantment.applySpeedModifier(entity, agilityLevel);
        } else {
            AgilityEnchantment.removeSpeedModifier(entity);
        }

        if (entity instanceof Player player) {
            ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
            int vitalityLevel = ModEnchantments.level(ModEnchantments.VITALITY, chestArmor);
            VitalityEnchantment.healPlayer(player, vitalityLevel);

            if (player.getMainHandItem().getItem() instanceof NunchakuItem) {
                player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                    if (combo.getComboTime() > 0 || combo.getComboPower() > 0) {
                        combo.countDown();
                    }
                });
            }
        }

        if (entity.hasEffect(ModMobEffects.STUN.get())) {
            double y = entity.getDeltaMovement().y;
            entity.setDeltaMovement(0, y <= 0 ? y : 0, 0);
            if (entity instanceof Creeper creeper) {
                creeper.setSwellDir(-1);
            }
        }

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

        // 以太·护盾粒子：持握且护盾生效时撒粒子（内部按 tick 节流、按强度缩放）
        if (BetterSurvival.isEnigmaticLoaded
                && entity.getMainHandItem().getItem() instanceof CustomWeaponItem weapon
                && com.mx.bettersurvival.integration.EnigmaticCompat.isEtheriumTier(weapon.getTier())) {
            com.mx.bettersurvival.integration.EnigmaticCompat.spawnShieldParticles(entity);
        }
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
        int level = ModEnchantments.level(ModEnchantments.HIGH_JUMP, boots);
        if (level > 0) {
            HighJumpEnchantment.boostJump(entity, level);
        }

        if (entity.hasEffect(ModMobEffects.STUN.get())) {
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1, 0, 1));
        }
    }

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

    // Weapon bonuses go through Progressive Mechanics Library's damage pipeline when it's tracking the hit,
    // so they stack with Spartan Weaponry traits and JLME enchantments by the same rules; otherwise edit the
    // event's amount like before.
    private static void addFlatDamage(LivingHurtEvent event, float bonus) {
        if (!(BetterSurvival.isPmlLoaded
                && com.mx.bettersurvival.integration.PmlCompat.addBaseFlatBonus(event.getSource(), bonus)))
            event.setAmount(event.getAmount() + bonus);
    }

    private static void multiplyDamage(LivingHurtEvent event, float multiplier) {
        if (!(BetterSurvival.isPmlLoaded
                && com.mx.bettersurvival.integration.PmlCompat.addBaseMultiplier(event.getSource(), multiplier)))
            event.setAmount(event.getAmount() * multiplier);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player attacker))
            return;
        if (attacker.level().isClientSide)
            return;

        LivingEntity target = event.getEntity();
        ItemStack weapon = attacker.getMainHandItem();

        // 过滤非直接近战伤害（如Melt/Frostbite的indirectMagic tick），
        // 避免药效持续伤害被误判为玩家攻击从而叠加/串联元素效果
        // Also counts other melee damage types listed in PML's pml:is_melee_attack tag (see MeleeHits).
        boolean isDirectAttack = com.mx.bettersurvival.combat.MeleeHits.isMeleeHit(event.getSource());

        // Dagger backstab: every dagger does bonus damage from behind (as its tooltip says); Assassinate adds
        // +0.5x per level on top. Previously the whole backstab was gated behind Assassinate, so plain daggers
        // never backstabbed.
        if (weapon.getItem() instanceof DaggerItem dagger && isDirectAttack) {
            float backstabMult = dagger.getBackstabMultiplier(attacker, target);
            if (backstabMult > 1.0F) {
                int level = ModEnchantments.level(ModEnchantments.ASSASSINATE, weapon);
                multiplyDamage(event, backstabMult + level * 0.5F);
            }
        }

        if (weapon.getItem() instanceof HammerItem && isDirectAttack) {
            int level = ModEnchantments.level(ModEnchantments.BASH, weapon);
            double stunChance = ModConfig.COMMON.stunBaseChance.get()
                    + level * ModConfig.COMMON.bashModifier.get();
            if (attacker.getAttackStrengthScale(0.5F) > 0.9F
                    && attacker.getRandom().nextFloat() < stunChance
                    && ModMobEffects.STUN.isPresent()) {
                target.addEffect(new MobEffectInstance(
                        ModMobEffects.STUN.get(), HammerItem.STUN_DURATION));
            }
        }

        if (weapon.getItem() instanceof BattleAxeItem && isDirectAttack) {
            // disarmBaseChance applies to every full-strength battle axe hit (like stunBaseChance for hammers);
            // Disarm only raises it. The old "level > 0" gate made base battle axes unable to disarm at all.
            int level = ModEnchantments.level(ModEnchantments.DISARM, weapon);
            if (attacker.getAttackStrengthScale(0.5F) > 0.9F) {
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

        if (isDirectAttack) {
            VampirismEnchantment.tryHeal(attacker, ModEnchantments.level(ModEnchantments.VAMPIRISM, weapon));
        }

        if (weapon.getItem() instanceof NunchakuItem && isDirectAttack) {
            attacker.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                multiplyDamage(event, combo.getComboPower() + 1.0F);
            });
        }

        if (isDirectAttack && BetterSurvival.isIafLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            float bonus = com.mx.bettersurvival.integration.IaFCompat.getMaterialModifier(weapon, target, attacker);
            if (bonus > 0.0F) {
                addFlatDamage(event, bonus);
            }
        }

        // 暮色森林：赤铁点燃、骑士金属破甲
        if (isDirectAttack && BetterSurvival.isTwilightLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            float bonus = com.mx.bettersurvival.integration.TwilightCompat.getMaterialModifier(weapon, target, attacker);
            if (bonus > 0.0F) {
                addFlatDamage(event, bonus);
            }
        }

        // 灾变：腾炎烙印、凋零、咒魂之怒（特效走状态效果，直接调灾变原生+原版）
        if (isDirectAttack && BetterSurvival.isCataclysmLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            com.mx.bettersurvival.integration.CataclysmCompat.getMaterialModifier(weapon, target, attacker);
        }

        // SRP 风味（感知/觉知，常驻材料）：特效接口预留，见 SrpCompat
        if (isDirectAttack && weapon.getItem() instanceof CustomWeaponItem) {
            float bonus = com.mx.bettersurvival.integration.SrpCompat.getMaterialModifier(weapon, target, attacker);
            if (bonus > 0.0F) {
                addFlatDamage(event, bonus);
            }
        }

        if (isDirectAttack && weapon.getItem() instanceof CustomWeaponItem cw
                && cw.getTier() == com.mx.bettersurvival.init.ModTiers.CRYING_OBSIDIAN) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
        }

        if (isDirectAttack && BetterSurvival.isDefiledLoaded && weapon.getItem() instanceof CustomWeaponItem) {
            com.mx.bettersurvival.integration.DefiledCompat.applyOnHitEffect(
                    weapon, target, attacker, event.getAmount());
        }

        // 以太·命中抽血：把生命注入护盾的高风险高回报循环（详见 EnigmaticCompat）
        if (isDirectAttack && BetterSurvival.isEnigmaticLoaded
                && weapon.getItem() instanceof CustomWeaponItem etherWeapon
                && com.mx.bettersurvival.integration.EnigmaticCompat.isEtheriumTier(etherWeapon.getTier())) {
            com.mx.bettersurvival.integration.EnigmaticCompat.applyDrainOnHit(attacker);
        }
    }

    // 以太·渐变护盾：受击方持以太时按血量减伤/弹射免疫/击退攻击者。
    // 独立处理器：需读"受击方"主手，且要覆盖任意来源伤害（非仅玩家攻击），故不能并入 onLivingHurt。
    @SubscribeEvent
    public static void onEtheriumShield(LivingHurtEvent event) {
        if (!BetterSurvival.isEnigmaticLoaded)
            return;
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide)
            return;
        if (victim.getMainHandItem().getItem() instanceof CustomWeaponItem weapon
                && com.mx.bettersurvival.integration.EnigmaticCompat.isEtheriumTier(weapon.getTier())) {
            com.mx.bettersurvival.integration.EnigmaticCompat.applyShield(event);
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        ItemStack item = event.getItem();
        if (!(item.getItem() instanceof BowItem))
            return;

        int rapidFireLevel = ModEnchantments.level(ModEnchantments.RAPID_FIRE, item);
        if (rapidFireLevel <= 0)
            return;

        int reduction = RapidFireEnchantment.getChargeTimeReduction(
                entity, event.getDuration());
        if (reduction > 0) {
            event.setDuration(event.getDuration() - reduction);
        }
    }

    // LOWEST so a mod that cancels the shot does so first (a cancelled ArrowLooseEvent isn't passed on to us).
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return;

        ItemStack bow = event.getBow();
        Level level = player.level();

        float power = BowItem.getPowerForTime(event.getCharge());
        if (power < 0.1F)
            return;

        int multishotLevel = ModEnchantments.level(ModEnchantments.MULTISHOT, bow);
        if (multishotLevel > 0) {
            // The extra arrows copy the ammo the bow is shooting (getProjectile also finds ammo in quivers that hook
            // into it, like Spartan Weaponry's), so tipped/spectral/modded arrows fire extras of the same kind.
            // ArrowLooseEvent fires before the bow checks its ammo; with no ammo the bow only fires in creative or
            // with Infinity, which use plain arrows.
            ItemStack ammo = player.getProjectile(bow);
            boolean infinite = player.getAbilities().instabuild || bow.getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0;
            if (ammo.isEmpty()) {
                if (!infinite)
                    return;
                ammo = new ItemStack(Items.ARROW);
            }
            ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem a ? a : (ArrowItem) Items.ARROW;

            int extraArrows = multishotLevel * 2;
            for (int i = 0; i < extraArrows; i++) {

                float angleOffset = ((i / 2) + 1) * 10.0F * (i % 2 == 0 ? 1 : -1);

                // Built like BowItem.releaseUsing builds the real arrow (createArrow, then the bow's customArrow hook)
                AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
                if (bow.getItem() instanceof BowItem bowItem)
                    arrow = bowItem.customArrow(arrow);
                arrow.shootFromRotation(player, player.getXRot(),
                        player.getYRot() + angleOffset, 0.0F,
                        power * 3.0F, 1.0F);
                // Extras are free, so they can't be picked up (or recovered by Arrow Recovery)
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                int powerLevel = bow.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(
                            arrow.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
                }

                int punchLevel = bow.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                if (punchLevel > 0) {
                    arrow.setKnockback(punchLevel);
                }

                if (bow.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                    arrow.setSecondsOnFire(100);
                }

                arrow.setCritArrow(power >= 1.0F);

                level.addFreshEntity(arrow);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide)
            return;
        // Arrows already in the world re-join whenever their chunk loads; only newly fired arrows should get the
        // Range speed boost / Arrow Recovery, or they get boosted again from whatever bow the owner holds now.
        if (event.loadedFromDisk())
            return;
        if (!(event.getEntity() instanceof AbstractArrow arrow))
            return;
        if (!(arrow.getOwner() instanceof Player player))
            return;

        // Only arrows shot from the bow the player is drawing right now. BowItem.releaseUsing spawns the arrow while
        // the bow is still the item in use, so this is that bow. Thrown spears, Spartan throwing weapons, tridents and
        // crossbow bolts aren't shot from a drawn bow, so a bow held in the other hand no longer gives them its
        // Range speed or Arrow Recovery.
        ItemStack bow = player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY;
        if (!(bow.getItem() instanceof BowItem))
            return;

        int rangeLevel = ModEnchantments.level(ModEnchantments.RANGE, bow);
        if (rangeLevel > 0) {
            double multiplier = RangeEnchantment.getVelocityMultiplier();
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(multiplier));
        }

        int recoveryLevel = ModEnchantments.level(ModEnchantments.ARROW_RECOVERY, bow);
        if (recoveryLevel > 0) {
            arrow.getCapability(ModCapabilities.ARROW_PROPERTIES).ifPresent(props -> {
                props.setRecoveryLevel(recoveryLevel);
            });
        }
    }

    // LOWEST so a mod that cancels the hit (Ice and Fire lets arrows pass through ghosts) does so first; cancelled
    // events aren't passed on to us, so the arrow isn't recovered when it never really hit.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide)
            return;
        if (!(event.getEntity() instanceof AbstractArrow arrow))
            return;
        if (!(arrow.getOwner() instanceof Player player))
            return;
        if (event.getRayTraceResult().getType() != HitResult.Type.ENTITY)
            return;

        arrow.getCapability(ModCapabilities.ARROW_PROPERTIES).ifPresent(props -> {
            int level = props.getRecoveryLevel();
            // Only arrows the player could pick up anyway: Multishot's extra arrows and Infinity/creative arrows are
            // CREATIVE_ONLY, and recovering those created free arrows.
            if (level > 0 && arrow.pickup == AbstractArrow.Pickup.ALLOWED) {
                float chance = Math.min(1.0F, level * 0.33F);
                if (player.getRandom().nextFloat() < chance) {
                    // The arrow's own pickup item: a tipped arrow keeps its potion, a modded arrow stays that arrow
                    ItemStack recovered = ((AbstractArrowInvoker) arrow).bettersurvival$getPickupItem();
                    if (!recovered.isEmpty()) {
                        giveRecovered(player, recovered.copy());
                    }
                    arrow.discard();
                }
            }
        });
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide)
            return;

        BlockState state = event.getState();
        ItemStack tool = player.getMainHandItem();

        if (!isMiningTunneling) {
            int tunnelingLevel = ModEnchantments.level(ModEnchantments.TUNNELING, tool);
            if (tunnelingLevel > 0) {
                isMiningTunneling = true;
                try {
                    TunnelingEnchantment.mineManyBlocks(player, state, event.getPos());
                } finally {
                    isMiningTunneling = false;
                }
            }
        }

        int diamondsLevel = ModEnchantments.level(ModEnchantments.DIAMONDS, tool);
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

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        int level = ModEnchantments.level(ModEnchantments.VERSATILITY, tool);
        if (level > 0) {
            float modifier = VersatilityEnchantment.getSpeedModifier(
                    player, event.getOriginalSpeed());
            if (modifier > 1.0F) {
                event.setNewSpeed(event.getOriginalSpeed() + modifier);
            }
        }
    }

    /**
     * Hands a recovered arrow to the player the way picking it up would: pickup handlers get it first (Spartan
     * Weaponry quivers with Ammo Collect on take arrows from EntityItemPickupEvent), what they leave goes into the
     * inventory, and what doesn't fit drops at the player's feet. Not dropped on the ground first, because with
     * ItemPhysic's click-to-pick-up the player would have to pick every recovered arrow up by hand.
     */
    private static void giveRecovered(Player player, ItemStack stack) {
        // Carrier item for the event only; it is never added to the world
        ItemEntity carrier = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
        MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(player, carrier));
        ItemStack left = carrier.getItem();
        if (!left.isEmpty() && !player.getInventory().add(left)) {
            player.drop(left, false);
        }
    }

    private static boolean isStoneBlock(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.BASE_STONE_NETHER);
    }

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

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof Player player))
            return;
        if (player.level().isClientSide)
            return;

        // Only direct melee hits use the held weapon. LivingAttackEvent also fires for the player's arrows and thrown
        // spears (their getEntity() is the player too), which broke the spear in hand when a thrown spear landed and
        // spent potion-coating charges on bow shots.
        if (event.getSource().getDirectEntity() != player)
            return;

        ItemStack weapon = player.getMainHandItem();

        if (weapon.getItem() instanceof SpearItem spear) {
            if (!player.getAbilities().instabuild
                    && spear.breakChance() >= player.getRandom().nextFloat()) {
                weapon.shrink(1);
            }
        }

        if (ModConfig.COMMON.enableWeaponCoating.get()
                && weapon.hasTag() && weapon.getTag().contains("remainingPotionHits")) {
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

    /**
     * Anvils check {@code Enchantment.canEnchant}, which for Sweeping Edge just asks "is it a SwordItem?" — true now
     * that BS weapons extend SwordItem, and the item gets no say. BS weapons can't sweep, so refuse the combination.
     */
    @SubscribeEvent
    public static void onAnvilUpdate(net.minecraftforge.event.AnvilUpdateEvent event) {
        if (event.getLeft().getItem() instanceof CustomWeaponItem
                && EnchantmentHelper.getEnchantments(event.getRight()).containsKey(Enchantments.SWEEPING_EDGE)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEnderTeleport(net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event) {
        if (event.getEntityLiving().hasEffect(ModMobEffects.ANTIWARP.get())) {
            event.setCanceled(true);
        }
    }
}
