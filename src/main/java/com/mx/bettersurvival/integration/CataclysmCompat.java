package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * L_Ender's Cataclysm（灾变，软依赖）武器材料的唯一接缝。
 *
 * <p>严守"黄金法则"：所有碰灾变的代码只在此文件。5 种材料：远古金属/黑钢（灾变原档，纯数值）、
 * Ignitium 腾炎 / Cursium 咒魂 / Witherite 凋零（后期 Boss 材料，防火高耐久，带招牌特效）。
 *
 * <p>特效"能调现成就调现成"：Ignitium 调灾变原生 {@code cataclysm:blazing_brand}（烙印削甲），
 * Witherite 调原生 {@code cataclysm:stun}（眩晕）+ 原版凋零/再生，Cursium 用原版力量当"诅咒之怒"。
 * 装了灾变才有这些武器，故原生效果必然在。
 */
public final class CataclysmCompat {

    private static final String MODID = "cataclysm";

    private static Tier ANCIENT_METAL;
    private static Tier BLACK_STEEL;
    private static Tier CURSIUM;
    private static Tier IGNITIUM;
    private static Tier WITHERITE;

    private static boolean initialized = false;

    // 灾变原生效果懒解析缓存
    private static MobEffect blazingBrand;
    private static MobEffect stun;
    private static boolean effectsResolved = false;

    private CataclysmCompat() {
    }

    public record CataclysmTierEntry(Tier tier, String name) {
    }

    public static void init() {
        if (initialized)
            return;
        initialized = true;
        try {
            // 远古金属/黑钢：灾变原档数值（3/750/8.0/+2/25）
            ANCIENT_METAL = new ForgeTier(3, 750, 8.0F, 2.0F, 25,
                    BlockTags.NEEDS_IRON_TOOL, repair("ancient_metal_ingot"));
            BLACK_STEEL = new ForgeTier(3, 750, 8.0F, 2.0F, 25,
                    BlockTags.NEEDS_IRON_TOOL, repair("black_steel_ingot"));
            // 三招牌：后期 Boss 材料（代理数值，防火在注册处加）
            CURSIUM = new ForgeTier(4, 2600, 9.0F, 6.0F, 12,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("cursium_ingot"));
            IGNITIUM = new ForgeTier(4, 2600, 9.0F, 6.0F, 15,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("ignitium_ingot"));
            WITHERITE = new ForgeTier(4, 3000, 9.0F, 5.0F, 12,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("witherite_ingot"));
            BetterSurvival.LOGGER.info("Cataclysm tiers created successfully.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to create Cataclysm tiers: {}", e.getMessage());
            ANCIENT_METAL = BLACK_STEEL = CURSIUM = IGNITIUM = WITHERITE = null;
        }
    }

    private static Supplier<Ingredient> repair(String ingot) {
        return () -> {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, ingot));
            return item != null ? Ingredient.of(item) : Ingredient.EMPTY;
        };
    }

    public static List<CataclysmTierEntry> getCataclysmTierEntries() {
        init();
        List<CataclysmTierEntry> list = new ArrayList<>();
        if (ANCIENT_METAL != null)
            list.add(new CataclysmTierEntry(ANCIENT_METAL, "ancientmetal"));
        if (BLACK_STEEL != null)
            list.add(new CataclysmTierEntry(BLACK_STEEL, "blacksteel"));
        if (CURSIUM != null)
            list.add(new CataclysmTierEntry(CURSIUM, "cursium"));
        if (IGNITIUM != null)
            list.add(new CataclysmTierEntry(IGNITIUM, "ignitium"));
        if (WITHERITE != null)
            list.add(new CataclysmTierEntry(WITHERITE, "witherite"));
        return list;
    }

    /** 三招牌材料武器防火（掉岩浆不烧毁）。 */
    public static boolean isFireproofTier(Tier tier) {
        return tier == CURSIUM || tier == IGNITIUM || tier == WITHERITE;
    }

    private static void resolveEffectsIfNeeded() {
        if (effectsResolved)
            return;
        effectsResolved = true;
        blazingBrand = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(MODID, "blazing_brand"));
        stun = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(MODID, "stun"));
    }

    /** 叠层施加：已有则放大器+1（封顶 maxAmp），否则从 0 起；每次刷新时长。 */
    private static void stackEffect(LivingEntity entity, MobEffect effect, int maxAmp, int duration) {
        if (effect == null)
            return;
        MobEffectInstance cur = entity.getEffect(effect);
        int amp = cur != null ? Math.min(cur.getAmplifier() + 1, maxAmp) : 0;
        entity.addEffect(new MobEffectInstance(effect, duration, amp));
    }

    /**
     * 命中特效（招牌三材料）。返回附加伤害（本处均为 0，特效走状态效果）。
     */
    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, boolean applyEffects) {
        if (!applyEffects || !(stack.getItem() instanceof CustomWeaponItem weapon))
            return 0.0F;
        Tier tier = weapon.getTier();

        if (tier == IGNITIUM) {
            resolveEffectsIfNeeded();
            stackEffect(target, blazingBrand, 3, 240);       // 灾变原生·炽焰烙印(削甲)，叠4层
            target.setSecondsOnFire(5);
        } else if (tier == WITHERITE) {
            resolveEffectsIfNeeded();
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));   // 凋零Ⅱ
            target.setSecondsOnFire(5);
            if (attacker != null && attacker.getHealth() < attacker.getMaxHealth() * 0.5F
                    && attacker.getRandom().nextFloat() < 0.5F) {
                attacker.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1)); // 低血再生Ⅱ
            }
            if (stun != null && target.getRandom().nextFloat() < 0.2F) {
                target.addEffect(new MobEffectInstance(stun, 60, 0));             // 灾变原生·眩晕
            }
        } else if (tier == CURSIUM) {
            if (attacker != null) {
                stackEffect(attacker, MobEffects.DAMAGE_BOOST, 3, 110);           // 诅咒之怒=原版力量自叠
            }
        }
        return 0.0F;
    }

    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker) {
        return getMaterialModifier(stack, target, attacker, true);
    }

    public static void addMaterialTooltip(Tier tier, List<Component> tooltip) {
        if (tier == IGNITIUM) {
            tooltip.add(Component.translatable("cataclysm_weapon.ignitium.hurt").withStyle(ChatFormatting.GOLD));
        } else if (tier == CURSIUM) {
            tooltip.add(Component.translatable("cataclysm_weapon.cursium.hurt").withStyle(ChatFormatting.DARK_PURPLE));
        } else if (tier == WITHERITE) {
            tooltip.add(Component.translatable("cataclysm_weapon.witherite.hurt").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
