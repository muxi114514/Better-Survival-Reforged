package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
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

/**
 * 暮色森林（Twilight Forest，软依赖）武器材料的唯一接缝。
 *
 * <p>严守"黄金法则"：所有碰 TF 的代码（查锭物品、命中特效）只在此文件；其余代码零依赖。
 * 4 种材料：Ironwood 铁木 / Steeleaf 钢叶 / Knightmetal 骑士金属 / Fiery 赤铁，
 * 选材与数值参考 SpartanTwilight（钢叶耐久由 131 上调至 400）。
 *
 * <p>招牌特效：Fiery=命中点燃15秒 + 物品防火；Knightmetal=对披甲敌人额外伤害（护甲越厚越高）。
 */
public final class TwilightCompat {

    private static final String MODID = "twilightforest";

    private static Tier IRONWOOD;
    private static Tier STEELEAF;
    private static Tier KNIGHTMETAL;
    private static Tier FIERY;

    private static boolean initialized = false;

    private TwilightCompat() {
    }

    public record TwilightTierEntry(Tier tier, String name) {
    }

    public static void init() {
        if (initialized)
            return;
        initialized = true;
        try {
            IRONWOOD = new ForgeTier(2, 512, 6.5F, 2.0F, 25,
                    BlockTags.NEEDS_IRON_TOOL, repair("ironwood_ingot"));
            STEELEAF = new ForgeTier(3, 400, 8.0F, 3.0F, 9,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("steeleaf_ingot"));
            KNIGHTMETAL = new ForgeTier(3, 512, 8.0F, 3.0F, 8,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("knightmetal_ingot"));
            FIERY = new ForgeTier(4, 1024, 9.0F, 4.0F, 10,
                    BlockTags.NEEDS_DIAMOND_TOOL, repair("fiery_ingot"));
            BetterSurvival.LOGGER.info("Twilight Forest tiers created successfully.");
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Failed to create Twilight Forest tiers: {}", e.getMessage());
            IRONWOOD = STEELEAF = KNIGHTMETAL = FIERY = null;
        }
    }

    /** 懒查暮色锭作修复材料，未解析到时返回空。 */
    private static java.util.function.Supplier<Ingredient> repair(String ingot) {
        return () -> {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, ingot));
            return item != null ? Ingredient.of(item) : Ingredient.EMPTY;
        };
    }

    public static List<TwilightTierEntry> getTwilightTierEntries() {
        init();
        List<TwilightTierEntry> list = new ArrayList<>();
        if (IRONWOOD != null)
            list.add(new TwilightTierEntry(IRONWOOD, "ironwood"));
        if (STEELEAF != null)
            list.add(new TwilightTierEntry(STEELEAF, "steeleaf"));
        if (KNIGHTMETAL != null)
            list.add(new TwilightTierEntry(KNIGHTMETAL, "knightmetal"));
        if (FIERY != null)
            list.add(new TwilightTierEntry(FIERY, "fiery"));
        return list;
    }

    /** 赤铁武器需防火（物品掉进岩浆不烧毁）——注册时据此给 fireResistant 属性。 */
    public static boolean isFieryTier(Tier tier) {
        return tier == FIERY;
    }

    /**
     * 命中特效 + 附加伤害：
     * <ul>
     *   <li>Fiery：点燃目标 15 秒（applyEffects 时）；</li>
     *   <li>Knightmetal：对披甲敌人额外伤害，护甲越厚越高（上限 +6）。</li>
     * </ul>
     */
    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker, boolean applyEffects) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon))
            return 0.0F;
        Tier tier = weapon.getTier();

        if (tier == FIERY) {
            if (applyEffects) {
                target.setSecondsOnFire(15);
            }
        } else if (tier == KNIGHTMETAL) {
            float armor = target.getArmorValue();
            if (armor > 0.0F) {
                return Math.min(armor * 0.2F, 6.0F);
            }
        }
        return 0.0F;
    }

    public static float getMaterialModifier(ItemStack stack, LivingEntity target,
            @Nullable Player attacker) {
        return getMaterialModifier(stack, target, attacker, true);
    }

    public static void addMaterialTooltip(Tier tier, List<Component> tooltip) {
        if (tier == FIERY) {
            tooltip.add(Component.translatable("twilight_weapon.fiery.hurt")
                    .withStyle(ChatFormatting.GOLD));
        } else if (tier == KNIGHTMETAL) {
            tooltip.add(Component.translatable("twilight_weapon.knightmetal.hurt")
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
