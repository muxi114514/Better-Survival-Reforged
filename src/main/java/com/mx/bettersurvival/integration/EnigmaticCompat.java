package com.mx.bettersurvival.integration;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 神秘遗物（Enigmatic Legacy，软依赖）以太材质的唯一接缝。
 *
 * <p>严守"黄金法则"：所有碰 EL 的代码（查 {@code etherium_ingot}/{@code ender_rod}、
 * 以太护盾数值与逻辑）只出现在这一个文件里；其余代码对 EL 零依赖，只调本类静态方法。
 *
 * <p>以太护盾（完整移植自 EnigmaticSpartan 的 EtheriumShieldTrait，高风险高回报）：
 * 命中抽血 → 渐变护盾减伤 / 弹射物趋近免疫 / 击退攻击者 / +攻击距离 / 以太粒子。
 * 数值全部接入 {@link ModConfig}，可在配置文件内调整。
 */
public final class EnigmaticCompat {

    /** EL 原版 mod id（非 Plus 分叉）。EnigmaticSpartan 亦查此 id。 */
    public static final String MODID = "enigmaticlegacy";

    /** 精确的以太青色尘埃粒子（护盾强时的观感强化）。 */
    private static final DustParticleOptions ETHER_DUST =
            new DustParticleOptions(new Vector3f(0.28F, 0.95F, 0.86F), 1.0F);

    private static Ingredient repairIngredient;

    private EnigmaticCompat() {
    }

    public record EnigmaticTierEntry(Tier tier, String name) {
    }

    /** 仅在 EL 存在时返回以太档；否则空表 → 不注册任何以太武器。 */
    public static List<EnigmaticTierEntry> getEnigmaticTierEntries() {
        List<EnigmaticTierEntry> list = new ArrayList<>();
        if (BetterSurvival.isEnigmaticLoaded) {
            list.add(new EnigmaticTierEntry(EtheriumTier.INSTANCE, "etherium"));
        }
        return list;
    }

    public static boolean isEtheriumTier(Tier tier) {
        return tier == EtheriumTier.INSTANCE;
    }

    // ============================ 配置安全读取 ============================
    // Tier getter 可能在配置加载前（物品注册期）被调用，故统一走 isLoaded 守卫 + 默认兜底。

    private static double safeD(net.minecraftforge.common.ForgeConfigSpec.DoubleValue v, double def) {
        try {
            return ModConfig.COMMON_SPEC.isLoaded() ? v.get() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static int safeI(net.minecraftforge.common.ForgeConfigSpec.IntValue v, int def) {
        try {
            return ModConfig.COMMON_SPEC.isLoaded() ? v.get() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean safeB(net.minecraftforge.common.ForgeConfigSpec.BooleanValue v, boolean def) {
        try {
            return ModConfig.COMMON_SPEC.isLoaded() ? v.get() : def;
        } catch (Exception e) {
            return def;
        }
    }

    public static int cfgHarvestLevel() {
        return safeI(ModConfig.COMMON.etheriumHarvestLevel, 5);
    }

    public static int cfgDurability() {
        return safeI(ModConfig.COMMON.etheriumDurability, 8888);
    }

    public static float cfgSpeed() {
        return (float) safeD(ModConfig.COMMON.etheriumSpeed, 10.0);
    }

    public static float cfgAttackDamage() {
        return (float) safeD(ModConfig.COMMON.etheriumAttackDamage, 8.0);
    }

    public static int cfgEnchantability() {
        return safeI(ModConfig.COMMON.etheriumEnchantability, 35);
    }

    public static double cfgShieldThreshold() {
        return safeD(ModConfig.COMMON.etheriumShieldThreshold, 0.40);
    }

    public static double cfgShieldMaxReduction() {
        return safeD(ModConfig.COMMON.etheriumShieldMaxReduction, 0.50);
    }

    public static double cfgDrainPercent() {
        return safeD(ModConfig.COMMON.etheriumDrainPercent, 0.05);
    }

    public static boolean cfgDrainEnabled() {
        return safeB(ModConfig.COMMON.etheriumDrainEnabled, true);
    }

    public static double cfgReachBonus() {
        return safeD(ModConfig.COMMON.etheriumReachBonus, 1.0);
    }

    /** 懒查以太锭作修复材料；未解析到（EL 物品尚未注册）时每次重试，绝不缓存空值。 */
    public static Ingredient etheriumRepairIngredient() {
        if (repairIngredient == null) {
            Item ingot = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MODID, "etherium_ingot"));
            if (ingot != null) {
                repairIngredient = Ingredient.of(ingot);
            }
        }
        return repairIngredient == null ? Ingredient.EMPTY : repairIngredient;
    }

    // ============================ 以太护盾逻辑 ============================

    /**
     * 护盾强度 S：仅在 (阈值, 100%) 之间生效，越接近阈值越强；满血与 ≤阈值均为 0。
     * ≤阈值的深血防护忠实保留给 EL 以太护甲套，单武器不抢戏。
     */
    public static float shieldStrength(LivingEntity e) {
        float hp = e.getHealth() / e.getMaxHealth();
        float thr = (float) cfgShieldThreshold();
        if (hp >= 1.0F || hp <= thr) {
            return 0F;
        }
        return Mth.clamp((1F - hp) / (1F - thr), 0F, 1F);
    }

    /** 命中抽血：高于阈值时每击扣自身上限血，夹在阈值绝不自杀（把玩家推向低血强护盾区）。 */
    public static void applyDrainOnHit(LivingEntity attacker) {
        if (!cfgDrainEnabled()) {
            return;
        }
        float maxHp = attacker.getMaxHealth();
        float floor = maxHp * (float) cfgShieldThreshold();
        if (attacker.getHealth() > floor) {
            float drained = maxHp * (float) cfgDrainPercent();
            attacker.setHealth(Math.max(attacker.getHealth() - drained, floor));
        }
    }

    /** 渐变护盾：减伤随血量降低而增强，弹射物趋近免疫，并击退攻击者。仅服务端调用。 */
    public static void applyShield(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        float s = shieldStrength(victim);
        if (s <= 0F) {
            return;
        }

        DamageSource source = event.getSource();
        float remaining;
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            remaining = 1F - s;                                                 // s=1 时免疫弹射物
        } else {
            float fullRemain = (float) (1.0 - cfgShieldMaxReduction());         // 满护盾残留伤害
            remaining = 1F - (1F - fullRemain) * s;                             // 线性逼近满减伤
        }
        event.setAmount(Math.max(0F, event.getAmount() * remaining));

        Entity src = source.getEntity();
        if (src instanceof LivingEntity attacker && s > 0.05F) {
            Vec3 dir = victim.position().subtract(attacker.position()).normalize();
            attacker.knockback(0.75F * s, dir.x, dir.z);
            victim.level().playSound(null, victim.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.PLAYERS, 0.5F, 0.9F + s * 0.2F);
        }
    }

    /** 以太粒子：持握且护盾生效时，按强度缩放、每 3 tick 一波。仅服务端调用。 */
    public static void spawnShieldParticles(LivingEntity entity) {
        float s = shieldStrength(entity);
        if (s <= 0F || entity.tickCount % 3 != 0) {
            return;
        }
        if (entity.level() instanceof ServerLevel server) {
            double x = entity.getX();
            double y = entity.getY() + entity.getBbHeight() * 0.5;
            double z = entity.getZ();
            server.sendParticles(ParticleTypes.PORTAL, x, y, z, 1 + Math.round(s * 3), 0.4, 0.5, 0.4, 0.0);
            if (s > 0.5F) {
                server.sendParticles(ETHER_DUST, x, y, z, 1, 0.35, 0.45, 0.35, 0.0);
            }
        }
    }

    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!(stack.getItem() instanceof CustomWeaponItem weapon) || !isEtheriumTier(weapon.getTier())) {
            return;
        }
        tooltip.add(Component.translatable("enigmatic_weapon.etherium.shield").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("enigmatic_weapon.etherium.reach").withStyle(ChatFormatting.DARK_AQUA));
    }

    /** 配置驱动的以太 Tier：getter 实时读 {@link ModConfig}，规避构造时机陷阱。 */
    public static final class EtheriumTier implements Tier {
        public static final EtheriumTier INSTANCE = new EtheriumTier();

        private EtheriumTier() {
        }

        @Override
        public int getUses() {
            return cfgDurability();
        }

        @Override
        public float getSpeed() {
            return cfgSpeed();
        }

        @Override
        public float getAttackDamageBonus() {
            return cfgAttackDamage();
        }

        @Override
        public int getLevel() {
            return cfgHarvestLevel();
        }

        @Override
        public int getEnchantmentValue() {
            return cfgEnchantability();
        }

        @Override
        public Ingredient getRepairIngredient() {
            return etheriumRepairIngredient();
        }
    }
}
