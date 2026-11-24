package com.mx.bettersurvival.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Calls the protected {@code AbstractArrow.getPickupItem()}: the item a player gets when picking the arrow up
 * (a tipped arrow with its potion, a modded arrow type, ...). Arrow Recovery gives back that item instead of a plain
 * vanilla arrow.
 */
@Mixin(AbstractArrow.class)
public interface AbstractArrowInvoker {

    @Invoker("getPickupItem")
    ItemStack bettersurvival$getPickupItem();
}
