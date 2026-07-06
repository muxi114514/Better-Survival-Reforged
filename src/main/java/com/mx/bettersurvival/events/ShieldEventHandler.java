package com.mx.bettersurvival.events;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.enchantments.ReflectionEnchantment;
import com.mx.bettersurvival.init.ModEnchantments;
import com.mx.bettersurvival.init.ModItems;
import com.mx.bettersurvival.items.CustomShieldItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * 自定义盾牌的战斗逻辑（移植自 1.12 ItemCustomShield + 事件处理）。
 *
 * <ul>
 *   <li>主动格挡：{@link ShieldBlockEvent} 完全挡下正面可格挡伤害（与原版盾一致）、反击退攻击者、Reflection 反弹；</li>
 *   <li>被动减伤：{@link LivingHurtEvent} 持盾即减伤（可配）；SpellShield 额外抵挡魔法；</li>
 *   <li>重量：{@link LivingEvent.LivingTickEvent} 举盾时按重量施加移速惩罚、Heavy 抗击退，放下即移除。</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID)
public final class ShieldEventHandler {

    private static final UUID SHIELD_SPEED_UUID = UUID.fromString("d6107045-134f-4c54-a645-75c3ae5c7a27");
    private static final UUID SHIELD_KB_UUID = UUID.fromString("d6107045-134f-4c54-a645-75c0ae5c7a27");

    private ShieldEventHandler() {
    }

    /** 主动格挡：完全挡下正面可格挡伤害（与原版盾一致）+ 反击退 + Reflection 反弹。 */
    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        LivingEntity defender = event.getEntity();
        ItemStack shield = defender.getUseItem();
        if (!(shield.getItem() instanceof CustomShieldItem)) {
            return;
        }

        // 完全格挡：强制挡下全部可格挡伤害（原版盾行为，正面 100% 免伤）。
        // 显式写回 originalBlockedDamage，兼容其它战斗 mod 事先削减过 blockedDamage 的情况。
        event.setBlockedDamage(event.getOriginalBlockedDamage());

        if (defender.level().isClientSide) {
            return;
        }

        // BlockPower：按等级几率免除本次盾牌耐久损耗（满级必定不掉耐久，让盾更耐用）
        int blockPowerLvl = shield.getEnchantmentLevel(ModEnchantments.BLOCK_POWER.get());
        if (blockPowerLvl > 0) {
            int maxLvl = Math.max(1, ModConfig.COMMON.blockingPowerLevel.get());
            if (defender.getRandom().nextFloat() < (float) blockPowerLvl / maxLvl) {
                event.setShieldTakesDamage(false);
            }
        }

        // 近战：反击退攻击者 + Reflection 反弹
        DamageSource source = event.getDamageSource();
        Entity src = source.getEntity();
        if (src instanceof LivingEntity attacker && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            attacker.knockback(0.5F, defender.getX() - attacker.getX(), defender.getZ() - attacker.getZ());
            ReflectionEnchantment.reflectDamage(attacker, defender, shield);
        }
    }

    /** 被动减伤（持盾即生效）+ SpellShield 魔法减伤（举盾时）。 */
    @SubscribeEvent
    public static void onShieldPassive(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        ItemStack shield = getHeldShield(victim);
        if (shield.isEmpty()) {
            return;
        }
        DamageSource source = event.getSource();

        // 被动减伤：跳过无视无敌帧的伤害（虚空/命令击杀等），避免被用于苟命
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            float dr = shield.getItem() == ModItems.BIG_SHIELD.get()
                    ? ModConfig.COMMON.shieldBigPassiveDR.get().floatValue()
                    : ModConfig.COMMON.shieldSmallPassiveDR.get().floatValue();
            if (dr > 0.0F) {
                event.setAmount(event.getAmount() * (1.0F - dr));
            }
        }

        // SpellShield：举盾时部分抵挡魔法伤害（原版魔法通常无法格挡）
        int spellLvl = shield.getEnchantmentLevel(ModEnchantments.SPELL_SHIELD.get());
        if (spellLvl > 0 && victim.isBlocking() && victim.getUseItem() == shield
                && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC))) {
            float blockPower = ((CustomShieldItem) shield.getItem()).getBlockPower();
            float reduce = Math.min(blockPower * (spellLvl / 3.0F), 1.0F);
            event.setAmount(event.getAmount() * (1.0F - reduce));
        }
    }

    /** 举盾时按重量施加移速惩罚（Weightless 减轻）+ Heavy 抗击退；放下即移除。 */
    @SubscribeEvent
    public static void onShieldTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }

        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        AttributeInstance kb = entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        boolean blocking = entity.isBlocking()
                && entity.getUseItem().getItem() instanceof CustomShieldItem;

        if (blocking && ModConfig.COMMON.shieldWeightSlowdown.get()) {
            ItemStack shield = entity.getUseItem();
            CustomShieldItem custom = (CustomShieldItem) shield.getItem();
            int weightless = shield.getEnchantmentLevel(ModEnchantments.WEIGHTLESS.get());
            int effWeight = Math.max(custom.getWeight() - 2 * weightless, 0);
            double mult = Math.pow(2, 1 - effWeight) - 1; // 越重越慢；effWeight<=1 时为 0（无惩罚）
            ensureModifier(speed, SHIELD_SPEED_UUID, "bettersurvival:shield_weight", mult,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);

            boolean heavy = shield.getEnchantmentLevel(ModEnchantments.HEAVY.get()) > 0;
            if (heavy) {
                ensureModifier(kb, SHIELD_KB_UUID, "bettersurvival:shield_heavy", 1.0,
                        AttributeModifier.Operation.ADDITION);
            } else {
                removeModifier(kb, SHIELD_KB_UUID);
            }
        } else {
            removeModifier(speed, SHIELD_SPEED_UUID);
            removeModifier(kb, SHIELD_KB_UUID);
        }
    }

    private static ItemStack getHeldShield(LivingEntity entity) {
        if (entity.getMainHandItem().getItem() instanceof CustomShieldItem) {
            return entity.getMainHandItem();
        }
        if (entity.getOffhandItem().getItem() instanceof CustomShieldItem) {
            return entity.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static void ensureModifier(AttributeInstance attr, UUID id, String name, double amount,
            AttributeModifier.Operation op) {
        if (attr == null) {
            return;
        }
        AttributeModifier existing = attr.getModifier(id);
        if (existing == null || existing.getAmount() != amount) {
            attr.removeModifier(id);
            attr.addTransientModifier(new AttributeModifier(id, name, amount, op));
        }
    }

    private static void removeModifier(AttributeInstance attr, UUID id) {
        if (attr != null && attr.getModifier(id) != null) {
            attr.removeModifier(id);
        }
    }
}
