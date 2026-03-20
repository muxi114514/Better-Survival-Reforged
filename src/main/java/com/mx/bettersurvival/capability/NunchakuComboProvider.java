package com.mx.bettersurvival.capability;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provider that attaches {@link INunchakuCombo} capability to player entities.
 * No NBT serialization — combo state is transient and resets on reload.
 */
public class NunchakuComboProvider implements ICapabilityProvider {

    private final NunchakuComboImpl instance = new NunchakuComboImpl();
    private final LazyOptional<INunchakuCombo> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.NUNCHAKU_COMBO) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    public void invalidate() {
        optional.invalidate();
    }
}
