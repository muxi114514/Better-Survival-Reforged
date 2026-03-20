package com.chen1335.ultimateEnchantment.enchantment.config;

import com.chen1335.ultimateEnchantment.enchantment.CommonEnchantmentBase;
import dev.shadowsoffire.placebo.config.Configuration;

import java.util.HashSet;
import java.util.Set;

public class EnchantmentConfig {
    public static Set<CommonEnchantmentBase> sets = new HashSet<>();

    public static void load(Configuration config) {
        sets.forEach(commonEnchantmentBase -> commonEnchantmentBase.loadConfig(config));
    }

}
