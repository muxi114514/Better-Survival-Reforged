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

/**
 * Flying spear projectile entity – ported from 1.12's EntityFlyingSpear.
 * <p>
 * Extends AbstractArrow so it inherits vanilla arrow physics (gravity, ground
 * embedding, pickup). Syncs the spear ItemStack via EntityDataAccessor so
 * the renderer can display the correct item model.
 */
public class FlyingSpearEntity extends AbstractArrow {

    /** Synced data: the spear ItemStack being thrown. */
    private static final EntityDataAccessor<ItemStack> DATA_SPEAR = SynchedEntityData.defineId(FlyingSpearEntity.class,
            EntityDataSerializers.ITEM_STACK);

    // ======================== Constructors ========================

    /** Required by EntityType deserialization. */
    public FlyingSpearEntity(EntityType<? extends FlyingSpearEntity> type, Level level) {
        super(type, level);
    }

    /** Convenience constructor used when a player throws. */
    public FlyingSpearEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FLYING_SPEAR.get(), shooter, level);
        if (shooter instanceof Player) {
            this.pickup = Pickup.ALLOWED;
        }
    }

    // ======================== Spear data ========================

    public void setSpear(ItemStack spear) {
        this.entityData.set(DATA_SPEAR, spear.copy());
    }

    public ItemStack getSpear() {
        return this.entityData.get(DATA_SPEAR);
    }

    // ======================== Data registration ========================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPEAR, ItemStack.EMPTY);
    }

    // ======================== Hit logic ========================

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Skip endermen (they teleport away from projectiles anyway)
        if (result.getEntity() instanceof EnderMan) {
            super.onHitEntity(result);
            return;
        }

        // Apply IaF CE material damage bonus for thrown spears (damage only, no
        // effects)
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

        // Let AbstractArrow handle damage etc.
        super.onHitEntity(result);

        // After hit processing, if server side, try to embed the spear in the entity
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity living) {
            ItemStack spearStack = this.getSpear();
            if (!spearStack.isEmpty() && spearStack.getItem() instanceof SpearItem spearItem) {

                if (this.pickup == Pickup.ALLOWED) {
                    // Check break chance – if spear survives, store it in the entity
                    if (spearItem.breakChance() < this.random.nextFloat()) {
                        // Try to store in entity capability
                        living.getCapability(ModCapabilities.SPEARS_IN).ifPresent(cap -> {
                            if (living.isAlive()) {
                                cap.addSpear(spearStack.copy());
                            } else {
                                // Entity died from this hit → just drop
                                living.spawnAtLocation(spearStack.copy(), 0.1F);
                            }
                        });
                    }
                    // else: spear broke on impact, nothing to recover
                }
            }
        }
    }

    @Override
    @Nonnull
    protected net.minecraft.sounds.SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    // ======================== Pickup item ========================

    @Nonnull
    @Override
    protected ItemStack getPickupItem() {
        ItemStack spear = this.getSpear();
        return spear.isEmpty() ? ItemStack.EMPTY : spear.copy();
    }

    // ======================== NBT persistence ========================

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
