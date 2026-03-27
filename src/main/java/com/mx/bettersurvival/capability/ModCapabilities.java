package com.mx.bettersurvival.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<IArrowProperties> ARROW_PROPERTIES = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<INunchakuCombo> NUNCHAKU_COMBO = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<ISpearsIn> SPEARS_IN = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ModCapabilities() {
    }
}
