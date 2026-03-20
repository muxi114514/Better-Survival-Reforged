package com.chen1335.ultimateEnchantment.enchantment.config;

import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import dev.shadowsoffire.placebo.config.Configuration;

public class SimpleEnchantmentInfo {

    public boolean isTradeable;

    public boolean isTreasureOnly;

    public boolean isDiscoverable;

    public SimpleEnchantmentInfo(CommonEnchantmentBase commonEnchantmentBase, Configuration config) {
        isTradeable = commonEnchantmentBase.isTradeable();
        isTradeable = config.getBoolean("isTradeable", commonEnchantmentBase.getClass().getSimpleName() + ".info", isTradeable, "isTradeable");

        isTreasureOnly = commonEnchantmentBase.isTreasureOnly();
        isTreasureOnly = config.getBoolean("isTreasureOnly", commonEnchantmentBase.getClass().getSimpleName() + ".info", isTreasureOnly, "isTreasureOnly");

        isDiscoverable = commonEnchantmentBase.isDiscoverable();
        isDiscoverable = config.getBoolean("isDiscoverable", commonEnchantmentBase.getClass().getSimpleName() + ".info", isDiscoverable, "isDiscoverable");

    }

    public SimpleEnchantmentInfo(boolean isTradeable, boolean isTreasureOnly, boolean isDiscoverable) {
        this.isTradeable = isTradeable;
        this.isTreasureOnly = isTreasureOnly;
        this.isDiscoverable = isDiscoverable;
    }
}
