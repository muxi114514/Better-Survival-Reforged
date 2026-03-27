package com.mx.bettersurvival.capability;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface ISpearsIn {

    void addSpear(ItemStack spear);

    List<ItemStack> getSpearsIn();

    void clearSpears();
}
