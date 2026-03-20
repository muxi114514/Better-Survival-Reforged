package com.mx.bettersurvival.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider that attaches {@link IArrowProperties} capability to arrow entities.
 * Implements serialization so the recovery level persists if the arrow is
 * saved/loaded.
 */
public class ArrowPropertiesProvider implements ICapabilitySerializable<CompoundTag> {

    private final ArrowPropertiesImpl instance = new ArrowPropertiesImpl();
    private final LazyOptional<IArrowProperties> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.ARROW_PROPERTIES) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        instance.deserializeNBT(nbt);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
