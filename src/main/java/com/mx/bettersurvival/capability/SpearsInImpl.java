package com.mx.bettersurvival.capability;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link ISpearsIn}.
 * Stores spear ItemStacks in a simple ArrayList.
 */
public class SpearsInImpl implements ISpearsIn {

    private final List<ItemStack> spears = new ArrayList<>();

    @Override
    public void addSpear(ItemStack spear) {
        spears.add(spear);
    }

    @Override
    public List<ItemStack> getSpearsIn() {
        return spears;
    }

    @Override
    public void clearSpears() {
        spears.clear();
    }
}
