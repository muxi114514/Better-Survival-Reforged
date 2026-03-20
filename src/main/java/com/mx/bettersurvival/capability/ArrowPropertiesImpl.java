package com.mx.bettersurvival.capability;

import net.minecraft.nbt.CompoundTag;

/**
 * Default implementation of {@link IArrowProperties}.
 */
public class ArrowPropertiesImpl implements IArrowProperties {

    private int recoveryLevel = 0;

    @Override
    public int getRecoveryLevel() {
        return recoveryLevel;
    }

    @Override
    public void setRecoveryLevel(int level) {
        this.recoveryLevel = level;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("RecoveryLevel", recoveryLevel);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        recoveryLevel = tag.getInt("RecoveryLevel");
    }
}
