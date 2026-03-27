package com.mx.bettersurvival.entities;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.init.ModEntities;
import com.mx.bettersurvival.items.SpearItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;

public class FlyingSpearEntity extends AbstractArrow {

    private static final EntityDataAccessor<ItemStack> DATA_SPEAR = SynchedEntityData.defineId(FlyingSpearEntity.class,
            EntityDataSerializers.ITEM_STACK);

    public FlyingSpearEntity(EntityType<? extends FlyingSpearEntity> type, Level level) {
        super(type, level);
    }

    public FlyingSpearEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FLYING_SPEAR.get(), shooter, level);
        if (shooter instanceof Player) {
            this.pickup = Pickup.ALLOWED;
        }
    }

    public void setSpear(ItemStack spear) {
        this.entityData.set(DATA_SPEAR, spear.copy());
    }

    public ItemStack getSpear() {
        return this.entityData.get(DATA_SPEAR);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPEAR, ItemStack.EMPTY);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {

        if (result.getEntity() instanceof EnderMan) {
            super.onHitEntity(result);
            return;
        }

        if (BetterSurvival.isIafLoaded && !this.level().isClientSide
                && result.getEntity() instanceof LivingEntity living) {
            ItemStack spearStack = this.getSpear();
            if (!spearStack.isEmpty()) {
                float bonus = com.mx.bettersurvival.integration.IaFCompat.getMaterialModifier(
                        spearStack, living, null, false);
                if (bonus > 0.0F) {
                    this.setBaseDamage(this.getBaseDamage() + bonus);
                }
            }
        }

        super.onHitEntity(result);

        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity living) {
            ItemStack spearStack = this.getSpear();
            if (!spearStack.isEmpty() && spearStack.getItem() instanceof SpearItem spearItem) {

                if (this.pickup == Pickup.ALLOWED) {

                    if (spearItem.breakChance() < this.random.nextFloat()) {

                        living.getCapability(ModCapabilities.SPEARS_IN).ifPresent(cap -> {
                            if (living.isAlive()) {
                                cap.addSpear(spearStack.copy());
                            } else {

                                living.spawnAtLocation(spearStack.copy(), 0.1F);
                            }
                        });
                    }

                }
            }
        }
    }

    @Override
    @Nonnull
    protected net.minecraft.sounds.SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Nonnull
    @Override
    protected ItemStack getPickupItem() {
        ItemStack spear = this.getSpear();
        return spear.isEmpty() ? ItemStack.EMPTY : spear.copy();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.put("Spear", this.getSpear().save(new CompoundTag()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Spear")) {
            this.setSpear(ItemStack.of(compound.getCompound("Spear")));
        }
    }
}
