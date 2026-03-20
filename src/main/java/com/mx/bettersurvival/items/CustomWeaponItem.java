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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all BetterSurvival custom weapons.
 * Replaces the 1.12.2 ItemCustomWeapon which extended Item with manual
 * attribute handling.
 */
public class CustomWeaponItem extends Item {

    protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private final float attackDamage;
    private final double attackSpeed;
    private final Tier tier;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public CustomWeaponItem(Tier tier, float damageModifier, float delayModifier, Properties properties) {
        super(properties.durability(tier.getUses()));
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
        this.defaultModifiers = builder.build();
    }

    public Tier getTier() {
        return this.tier;
    }

    public float getAttackDamage() {
        return this.attackDamage;
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

    /**
     * Allows sword enchantments to be applied at the enchanting table.
     * Excludes Sweeping Edge.
     */
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment != Enchantments.SWEEPING_EDGE && enchantment.category == EnchantmentCategory.WEAPON) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        addVenomTooltip(stack, tooltip);
        // IaF CE material tooltip
        if (BetterSurvival.isIafLoaded) {
            com.mx.bettersurvival.integration.IaFCompat.addMaterialTooltip(this.tier, tooltip);
        }
        // Defiled Lands material tooltip
        if (BetterSurvival.isDefiledLoaded) {
            com.mx.bettersurvival.integration.DefiledCompat.appendTooltip(stack, tooltip);
        }
    }

    /**
     * Adds venom tooltip lines to any weapon. Can be called from external tooltip
     * events too.
     */
    public static void addVenomTooltip(ItemStack stack, List<Component> tooltip) {
        if (!stack.hasTag() || !stack.getTag().contains("remainingPotionHits"))
            return;
        int remaining = stack.getTag().getInt("remainingPotionHits");
        if (remaining <= 0)
            return;

        net.minecraft.world.item.alchemy.Potion potion = net.minecraft.world.item.alchemy.PotionUtils.getPotion(stack);
        if (potion == net.minecraft.world.item.alchemy.Potions.EMPTY)
            return;

        // Get potion display name from its first effect
        String potionName;
        if (!potion.getEffects().isEmpty()) {
            potionName = Component.translatable(potion.getEffects().get(0).getDescriptionId()).getString();
        } else {
            potionName = potion.getName("");
        }

        // Max hits = 50 if stacked, 10 for a single application
        // We track actual max by ceil to nearest 10
        int maxHits = ((remaining + 9) / 10) * 10;
        if (maxHits < 10)
            maxHits = 10;
        if (maxHits > 50)
            maxHits = 50;

        tooltip.add(Component.literal("☠ " + potionName + " (" + remaining + "/" + maxHits + ")")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
