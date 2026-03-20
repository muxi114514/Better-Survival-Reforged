package com.mx.bettersurvival.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

/**
 * Central registry of all BetterSurvival capabilities.
 */
public class ModCapabilities {

    /** Stores ArrowRecovery enchantment level on arrow entities. */
    public static final Capability<IArrowProperties> ARROW_PROPERTIES = CapabilityManager.get(new CapabilityToken<>() {
    });

    /** Stores nunchaku combo state on player entities. */
    public static final Capability<INunchakuCombo> NUNCHAKU_COMBO = CapabilityManager.get(new CapabilityToken<>() {
    });

    /** Tracks spears stuck in living entities. */
    public static final Capability<ISpearsIn> SPEARS_IN = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ModCapabilities() {
    }
}
