package com.mx.bettersurvival.capability;

/**
 * Capability interface attached to arrow entities to store
 * the ArrowRecovery enchantment level from the bow that shot them.
 */
public interface IArrowProperties {

    /** Get the ArrowRecovery enchantment level stored on this arrow. */
    int getRecoveryLevel();

    /** Set the ArrowRecovery enchantment level (called when the arrow is shot). */
    void setRecoveryLevel(int level);
}
