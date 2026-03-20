package com.mx.bettersurvival.capability;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Capability interface for tracking spears stuck in a living entity.
 * When the entity dies, all stored spears are dropped as item entities.
 */
public interface ISpearsIn {

    /** Add a spear ItemStack to this entity's stuck-spear list. */
    void addSpear(ItemStack spear);

    /** Get all spears currently stuck in this entity. */
    List<ItemStack> getSpearsIn();

    /** Clear all stored spears (used after dropping on death). */
    void clearSpears();
}
