package com.mx.bettersurvival.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.entities.FlyingSpearEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class SpearItem extends CustomWeaponItem {

    private static final UUID REACH_MODIFIER_UUID = UUID.fromString("f14d3a86-ef0c-48a7-a59f-b6650e6132f5");
    private final float reach;
    private final Multimap<Attribute, AttributeModifier> spearModifiers;

    public SpearItem(Tier tier, float damageModifier, float speedModifier, float reachBonus, Properties properties) {
        super(tier, damageModifier, speedModifier, properties.stacksTo(16));
        this.reach = reachBonus;

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier",
                        this.getAttackDamage(), AttributeModifier.Operation.ADDITION));
        builder.put(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier",
                        -2.4000000953674316 * speedModifier,
                        AttributeModifier.Operation.ADDITION));
        builder.put(ForgeMod.ENTITY_REACH.get(),
                new AttributeModifier(REACH_MODIFIER_UUID, "bettersurvival:spear_reach",
                        this.reach, AttributeModifier.Operation.ADDITION));
        this.spearModifiers = builder.build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.spearModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean isCreative = player.getAbilities().instabuild;

        if (!level.isClientSide) {
            FlyingSpearEntity spearEntity = new FlyingSpearEntity(level, player);

            ItemStack singleSpear = itemstack.copy();
            singleSpear.setCount(1);
            spearEntity.setSpear(singleSpear);

            spearEntity.setBaseDamage(this.getAttackDamage());

            spearEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 1.0F);

            if (isCreative) {
                spearEntity.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }

            level.addFreshEntity(spearEntity);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);

        if (!isCreative) {
            itemstack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 0;
    }

    public float breakChance() {
        return 32.0f / this.getTier().getUses();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(BetterSurvival.MOD_ID + ".spear.desc")
                .withStyle(ChatFormatting.AQUA));
    }
}
