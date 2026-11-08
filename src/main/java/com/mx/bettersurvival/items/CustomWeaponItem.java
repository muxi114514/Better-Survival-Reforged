package com.mx.bettersurvival.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Base class for every Better Survival weapon (hammer, spear, dagger, battle axe, nunchaku).
 *
 * <p>Extends {@link SwordItem} (as the original 1.12 Better Survival did with ItemSword) because almost every
 * enchantment check in vanilla and in other mods is a type check: {@code EnchantmentCategory.WEAPON} is literally
 * {@code item instanceof SwordItem}, the anvil uses {@code Enchantment.canEnchant} (which goes through that
 * category, not through {@link #canApplyAtEnchantingTable}), and mods like Ultimate Enchantment / Apotheosis build
 * their own "melee weapon" categories on top of {@code EnchantmentCategory.WEAPON}. As a plain {@code Item} our
 * weapons failed all of those checks, so enchanted books could not be applied to them at an anvil and most modded
 * weapon enchantments were unavailable. Sweeping is explicitly turned off below to keep the original behaviour.
 */
public class CustomWeaponItem extends SwordItem {

    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    // 固定 UUID：以太攻击距离加成（主副手不叠加）
    private static final UUID ETHERIUM_REACH_UUID = UUID.fromString("b7e2a3c4-1d5e-4f60-8a91-2c3d4e5f6a70");

    private final float attackDamage;
    private final double attackSpeed;
    private final Tier tier;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public CustomWeaponItem(Tier tier, float damageModifier, float delayModifier, Properties properties) {
        // SwordItem/TieredItem sets durability from the tier and builds its own attribute map; that map is never
        // used because getDefaultAttributeModifiers is overridden below, and getDamage() is overridden too.
        super(tier, 0, 0.0F, properties);
        this.tier = tier;
        this.attackDamage = (3.0F + tier.getAttackDamageBonus()) * damageModifier;
        this.attackSpeed = -2.4000000953674316 * delayModifier;

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.attackDamage,
                        AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", this.attackSpeed,
                        AttributeModifier.Operation.ADDITION));

        // 以太·持握 +攻击距离（矛已自带 reach 并覆盖本方法，故此处只作用于锤/匕首/战斧/双截棍）
        if (com.mx.bettersurvival.integration.EnigmaticCompat.isEtheriumTier(tier)) {
            double reach = com.mx.bettersurvival.integration.EnigmaticCompat.cfgReachBonus();
            if (reach > 0) {
                builder.put(net.minecraftforge.common.ForgeMod.ENTITY_REACH.get(),
                        new AttributeModifier(ETHERIUM_REACH_UUID, "bettersurvival:etherium_reach", reach,
                                AttributeModifier.Operation.ADDITION));
            }
        }
        this.defaultModifiers = builder.build();
    }

    public Tier getTier() {
        return this.tier;
    }

    public float getAttackDamage() {
        return this.attackDamage;
    }

    /** SwordItem's own damage value (used e.g. by mobs deciding which weapon to pick up) — report our real damage. */
    @Override
    public float getDamage() {
        return this.attackDamage;
    }

    /** Better Survival weapons never did sweep attacks; SwordItem would otherwise grant SWORD_SWEEP. */
    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        if (toolAction == ToolActions.SWORD_SWEEP) {
            return false;
        }
        return super.canPerformAction(stack, toolAction);
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, (entity) -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner, (entity) -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        // Sweeping Edge does nothing without the sweep action, so keep it off the table.
        if (enchantment == Enchantments.SWEEPING_EDGE) {
            return false;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        addVenomTooltip(stack, tooltip);

        if (BetterSurvival.isIafLoaded) {
            com.mx.bettersurvival.integration.IaFCompat.addMaterialTooltip(this.tier, tooltip);
        }

        if (BetterSurvival.isTwilightLoaded) {
            com.mx.bettersurvival.integration.TwilightCompat.addMaterialTooltip(this.tier, tooltip);
        }

        if (BetterSurvival.isCataclysmLoaded) {
            com.mx.bettersurvival.integration.CataclysmCompat.addMaterialTooltip(this.tier, tooltip);
        }

        // SRP 风味（常驻）：tooltip 接口预留
        com.mx.bettersurvival.integration.SrpCompat.addMaterialTooltip(this.tier, tooltip);

        if (BetterSurvival.isDefiledLoaded) {
            com.mx.bettersurvival.integration.DefiledCompat.appendTooltip(stack, tooltip);
        }

        if (BetterSurvival.isEnigmaticLoaded) {
            com.mx.bettersurvival.integration.EnigmaticCompat.appendTooltip(stack, tooltip);
        }
    }

    public static void addVenomTooltip(ItemStack stack, List<Component> tooltip) {
        if (!stack.hasTag() || !stack.getTag().contains("remainingPotionHits"))
            return;
        int remaining = stack.getTag().getInt("remainingPotionHits");
        if (remaining <= 0)
            return;

        net.minecraft.world.item.alchemy.Potion potion = net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack);
        if (potion == net.minecraft.world.item.alchemy.Potions.EMPTY)
            return;

        String potionName;
        if (!potion.getEffects().isEmpty()) {
            potionName = Component.translatable(potion.getEffects().get(0).getDescriptionId()).getString();
        } else {
            potionName = potion.getName("");
        }

        int maxHits = ((remaining + 9) / 10) * 10;
        if (maxHits < 10)
            maxHits = 10;
        if (maxHits > 50)
            maxHits = 50;

        tooltip.add(Component.literal("☠ " + potionName + " (" + remaining + "/" + maxHits + ")")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
