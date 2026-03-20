package com.mx.bettersurvival.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability provider for {@link ISpearsIn}.
 * Attached to all LivingEntity instances via {@link CapabilityEventHandler}.
 * Serializes stuck spears as a ListTag of CompoundTags.
 */
public class SpearsInProvider implements ICapabilitySerializable<CompoundTag> {

    private final ISpearsIn instance = new SpearsInImpl();
    private final LazyOptional<ISpearsIn> lazy = LazyOptional.of(() -> instance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.SPEARS_IN) {
            return lazy.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack spear : instance.getSpearsIn()) {
            list.add(spear.save(new CompoundTag()));
        }
        root.put("Spears", list);
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        ListTag list = nbt.getList("Spears", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            ItemStack spear = ItemStack.of(tag);
            if (!spear.isEmpty()) {
                instance.addSpear(spear);
            }
        }
    }

    public void invalidate() {
        lazy.invalidate();
    }
}
