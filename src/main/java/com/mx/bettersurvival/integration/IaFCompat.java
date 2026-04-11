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
 * RLC IaF 兼容层 — 使用RLC版独有的效果系统:
 * 火系: MobEffectMelt（熔蚀叠层减甲）
 * 冰系: MobEffectFrostbite（冰噬叠层冰碎）
 * 电系: ChainLightningUtils + MobEffectVoltage（链闪+蓄电放电）
 * 金龙钢: 圣铭之力（魔法伤害+发光）
 */
public final class IaFCompat {

    private IaFCompat() {
    }

    // 基础材料Tier
    private static Tier COPPER;
    private static Tier SILVER;
    private static Tier DRAGONBONE;
    private static Tier FIRE_DRAGONBONE;
    private static Tier ICE_DRAGONBONE;
    private static Tier LIGHTNING_DRAGONBONE;

    // 龙钢Tier
    private static Tier FIRE_DRAGONSTEEL;
    private static Tier ICE_DRAGONSTEEL;
    private static Tier LIGHTNING_DRAGONSTEEL;
    private static Tier GOLD_DRAGONSTEEL;

    private static boolean initialized = false;

    public record IafTierEntry(Tier tier, String name) {
    }

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        // 基础材料Tier初始化
        try {
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

            BetterSurvival.LOGGER.info("RLC IaF tiers resolved successfully via ForgeTier proxy.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to resolve RLC IaF tiers: {}", e.getMessage());
            COPPER = SILVER = DRAGONBONE = FIRE_DRAGONBONE = ICE_DRAGONBONE = LIGHTNING_DRAGONBONE = null;
        }

        // 龙钢Tier初始化（含金龙钢）
        try {
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
            GOLD_DRAGONSTEEL = new net.minecraftforge.common.ForgeTier(
                    4, 4000, 4.0F, 10.0F, 10,
                    net.minecraft.tags.BlockTags.NEEDS_DIAMOND_TOOL,
                    () -> net.minecraft.world.item.crafting.Ingredient.EMPTY);

            BetterSurvival.LOGGER.info("RLC IaF Dragon Steel tiers created (including Gold).");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to create RLC IaF Dragon Steel tiers: {}", e.getMessage());
            FIRE_DRAGONSTEEL = ICE_DRAGONSTEEL = LIGHTNING_DRAGONSTEEL = GOLD_DRAGONSTEEL = null;
        }
    }

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
        if (GOLD_DRAGONSTEEL != null)
            list.add(new IafTierEntry(GOLD_DRAGONSTEEL, "golddragonsteel"));
        return list;
    }

    // ════════════════════════════════════════════════════════
    // 效果应用
    // ════════════════════════════════════════════════════════

    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, boolean applyEffects) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return 0.0F;
        Tier tier = weapon.getTier();
        float baseDamage = weapon.getAttackDamage();

        // 银 — 碎灵
        if (tier == SILVER) {
            if (target.getMobType() == MobType.UNDEAD)
                return 2.0F;
        }
        // 火龙骨 — 熔蚀
        else if (tier == FIRE_DRAGONBONE) {
            if (applyEffects)
                applyMeltEffect(target, attacker, baseDamage, 120);
            if (isIceDragon(target))
                return 8.0F;
        }
        // 冰龙骨 — 冰噬
        else if (tier == ICE_DRAGONBONE) {
            if (applyEffects)
                applyFrostbiteEffect(target, attacker, 200, baseDamage);
            if (isFireDragon(target))
                return 8.0F;
        }
        // 雷龙骨 — 链式闪电+蓄电
        else if (tier == LIGHTNING_DRAGONBONE) {
            if (applyEffects && attacker != null)
                applyChainLightning(target, attacker, baseDamage);
            if (isFireDragon(target) || isIceDragon(target))
                return 4.0F;
        }
        // 火龙钢 — 强化熔蚀
        else if (tier == FIRE_DRAGONSTEEL) {
            if (applyEffects)
                applyMeltEffect(target, attacker, baseDamage, 160);
            if (isIceDragon(target))
                return 12.0F;
        }
        // 冰龙钢 — 强化冰噬
        else if (tier == ICE_DRAGONSTEEL) {
            if (applyEffects)
                applyFrostbiteEffect(target, attacker, 200, baseDamage);
            if (isFireDragon(target))
                return 12.0F;
        }
        // 雷龙钢 — 强化链闪
        else if (tier == LIGHTNING_DRAGONSTEEL) {
            if (applyEffects && attacker != null)
                applyChainLightning(target, attacker, baseDamage);
            if (isFireDragon(target) || isIceDragon(target))
                return 6.0F;
        }
        // 金龙钢 — 圣铭之力
        else if (tier == GOLD_DRAGONSTEEL) {
            if (applyEffects) {
                applyGoldDragonsteelEffect(target, attacker, baseDamage);
            }
        }

        return 0.0F;
    }

    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker) {
        return getMaterialModifier(stack, target, attacker, true);
    }

    // ════════════════════════════════════════════════════════
    // RLC IaF效果 — 反射调用
    // ════════════════════════════════════════════════════════

    /**
     * 熔蚀效果 — 叠层减甲+持续魔法伤害
     * 反射调用: MobEffectMelt.applyMelt(target, attacker, weaponDamage, durationTicks)
     */
    private static void applyMeltEffect(LivingEntity target, @Nullable LivingEntity attacker,
            float weaponDamage, int durationTicks) {
        try {
            com.github.alexthe666.iceandfire.effect.MobEffectMelt.applyMelt(target, attacker, weaponDamage, durationTicks);
        } catch (Throwable e) {
            // 降级: 使用原版着火效果
            target.setSecondsOnFire(5);
            BetterSurvival.LOGGER.warn("RLC Melt fallback to setSecondsOnFire. Cause: {}", e.toString());
        }
    }

    /**
     * 冰噬效果 — 叠层到阈值触发冰碎爆发
     */
    private static void applyFrostbiteEffect(LivingEntity target, @Nullable LivingEntity attacker,
            int durationTicks, float baseDamage) {
        try {
            com.github.alexthe666.iceandfire.effect.MobEffectFrostbite.applyFrostbite(target, attacker, durationTicks, baseDamage);
        } catch (Throwable e) {
            // 降级: 使用原版缓慢效果
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 2));
            BetterSurvival.LOGGER.warn("RLC Frostbite fallback to slowness. Cause: {}", e.toString());
        }
    }

    /**
     * 链式闪电 — 多目标跳跃 + 蓄电叠层
     */
    private static void applyChainLightning(LivingEntity target, Entity attacker, float baseDamage) {
        if (target.level().isClientSide)
            return;

        try {
            com.github.alexthe666.iceandfire.api.ChainLightningUtils.createChainLightning(target.level(), target, attacker, baseDamage);
        } catch (Throwable e) {
            // 降级: JMixin链闪 或 原版闪电
            triggerFallbackLightning(target, attacker);
            BetterSurvival.LOGGER.warn("RLC ChainLightning fallback. Cause: {}", e.toString());
        }
    }

    /**
     * 闪电降级: JMixin → 原版LightningBolt
     */
    private static void triggerFallbackLightning(LivingEntity target, Entity attacker) {
        if (BetterSurvival.isJMixinLoaded) {
            try {
                com.mx.jmixin.lightning.ChainLightningUtils.createChainLightning(
                        target.level(), target, attacker);
                return;
            } catch (Exception ignored) {
            }
        }
        if (target.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.minecraft.world.entity.LightningBolt bolt =
                    net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (bolt != null) {
                bolt.moveTo(target.position());
                if (attacker instanceof net.minecraft.server.level.ServerPlayer sp) {
                    bolt.setCause(sp);
                }
                bolt.addTag("IceAndFire_DontDestroyLoot");
                serverLevel.addFreshEntity(bolt);
            }
        }
    }

    /**
     * 金龙钢 — 圣铭之力: 魔法伤害 + 正面效果增伤 + 发光
     */
    private static void applyGoldDragonsteelEffect(LivingEntity target,
            @Nullable LivingEntity attacker, float baseDamage) {
        float bonusDamage = 0;
        // 目标身上有正面效果时增伤30%
        if (target.getActiveEffects().stream().anyMatch(e -> e.getEffect().isBeneficial())) {
            bonusDamage = baseDamage * 0.3F;
        }
        if (attacker != null) {
            target.hurt(attacker.level().damageSources().magic(), baseDamage * 0.5F + bonusDamage);
        }
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
    }

    // ════════════════════════════════════════════════════════
    // 工具方法
    // ════════════════════════════════════════════════════════

    public static boolean isFireDragon(Entity entity) {
        return entity.getClass().getName().contains("EntityFireDragon");
    }

    public static boolean isIceDragon(Entity entity) {
        return entity.getClass().getName().contains("EntityIceDragon");
    }

    public static void addMaterialTooltip(Tier tier, List<Component> tooltip) {
        if (tier == SILVER) {
            tooltip.add(Component.translatable("silvertools.hurt")
                    .withStyle(ChatFormatting.GREEN));
        } else if (tier == DRAGONBONE) {
            // 普通龙骨无特殊提示
        } else if (tier == FIRE_DRAGONBONE || tier == FIRE_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_fire.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_fire.hurt2")
                    .withStyle(ChatFormatting.DARK_RED));
        } else if (tier == ICE_DRAGONBONE || tier == ICE_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_ice.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_ice.hurt2")
                    .withStyle(ChatFormatting.AQUA));
        } else if (tier == LIGHTNING_DRAGONBONE || tier == LIGHTNING_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt1")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("dragon_sword_lightning.hurt2")
                    .withStyle(ChatFormatting.DARK_PURPLE));
        } else if (tier == GOLD_DRAGONSTEEL) {
            tooltip.add(Component.translatable("dragon_sword_gold.hurt2")
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
