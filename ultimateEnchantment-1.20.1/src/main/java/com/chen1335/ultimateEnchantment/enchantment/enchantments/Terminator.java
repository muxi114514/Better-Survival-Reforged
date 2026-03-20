package com.chen1335.ultimateEnchantment.enchantment.enchantments;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.SingleAttributeEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.config.SimpleEnchantmentInfo;
import com.chen1335.ultimateEnchantment.utils.SimpleSchedule;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

public class Terminator extends SingleAttributeEnchantment {
    public static final int[] SIDES = new int[]{-1, 1};

    public Terminator() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND}, UltimateEnchantment.EnchantmentType.ULTIMATE_ENCHANTMENT, ALObjects.Attributes.DRAW_SPEED.get(), 0.5F, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifierHolder(new AttributeModifierHolder(ALObjects.Attributes.CRIT_CHANCE.get(), -0.75F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.addAttributeModifierHolder(new AttributeModifierHolder(ALObjects.Attributes.CRIT_DAMAGE.get(), -0.5F, AttributeModifier.Operation.MULTIPLY_TOTAL));
        this.setInfo(new SimpleEnchantmentInfo(false, false, true));
    }

    public int additionalShotPerSide = 2;

    public void shoot(ItemStack pStack, Level pLevel, LivingEntity livingEntity, int pTimeLeft) {
        this.multipleShooting(pStack, pLevel, livingEntity, pTimeLeft);
        SimpleSchedule.addSchedule(pLevel, new SimpleSchedule.Wait(() -> this.SingleShoot(pStack, pLevel, livingEntity, pTimeLeft), 5));
    }

    public void SingleShoot(ItemStack pStack, Level pLevel, LivingEntity livingEntity, int pTimeLeft) {
        ItemStack oldUseItem = livingEntity.getUseItem();
        livingEntity.useItem = pStack;
        pStack.releaseUsing(pLevel, livingEntity, pTimeLeft);
        this.multipleShooting(pStack, pLevel, livingEntity, pTimeLeft);
        livingEntity.useItem = oldUseItem;
    }

    public void multipleShooting(ItemStack pStack, Level pLevel, LivingEntity livingEntity, int pTimeLeft) {
        for (int side : SIDES) {
            for (int i = 0; i < additionalShotPerSide; i++) {
                ItemStack itemstack = livingEntity.getProjectile(pStack).copy();
                int useTime = pStack.getUseDuration() - pTimeLeft;
                int j = useTime;
                if (livingEntity instanceof Player player) {
                    j = ForgeEventFactory.onArrowLoose(pStack, pLevel, player, useTime, !itemstack.isEmpty());
                }
                if (j >= 0) {
                    if (itemstack.isEmpty()) {
                        itemstack = new ItemStack(Items.ARROW);
                    }
                    ArrowItem arrowitem = (ArrowItem) (itemstack.getItem() instanceof ArrowItem ? itemstack.getItem() : Items.ARROW);
                    AbstractArrow abstractarrow = arrowitem.createArrow(pLevel, itemstack, livingEntity);
                    if (pStack.getItem() instanceof BowItem bowItem) {
                        abstractarrow = bowItem.customArrow(abstractarrow);
                    }
                    float f = BowItem.getPowerForTime(j);
                    abstractarrow.shootFromRotation(livingEntity, livingEntity.getXRot(), livingEntity.getYRot() + ((float) 5 / additionalShotPerSide) * side, 0.0F, f * 3.0F, 1.0F);


                    if (f == 1.0F) {
                        abstractarrow.setCritArrow(true);
                    }

                    int power = pStack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
                    if (power > 0) {
                        abstractarrow.setBaseDamage(abstractarrow.getBaseDamage() + (double) power * 0.5 + 0.5);
                    }

                    int punch = pStack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
                    if (punch > 0) {
                        abstractarrow.setKnockback(punch);
                    }

                    if (pStack.getEnchantmentLevel(Enchantments.FLAMING_ARROWS) > 0) {
                        abstractarrow.setSecondsOnFire(100);
                    }

                    abstractarrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    pLevel.addFreshEntity(abstractarrow);
                }
            }
        }
    }

    @Override
    public int getMinCost(int pLevel) {
        return 50;
    }
}
