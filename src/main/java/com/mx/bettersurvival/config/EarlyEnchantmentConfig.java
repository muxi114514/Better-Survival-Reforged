package com.mx.bettersurvival.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mx.bettersurvival.BetterSurvival;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads the enchantment max levels straight from config/bettersurvival-common.toml while the mod is
 * being constructed. Forge fires the registry events before it loads COMMON configs, so ModConfig.COMMON
 * has no values yet when enchantments are registered; this is what lets a max level of 0 keep an
 * enchantment out of the registry completely.
 */
public final class EarlyEnchantmentConfig {

    private static final String FILE = "bettersurvival-common.toml";

    private static boolean loaded;
    // null: no file yet (first launch) or it couldn't be read, so every enchantment stays on (the defaults)
    private static CommentedConfig values;

    private EarlyEnchantmentConfig() {
    }

    private static void load() {
        loaded = true;
        Path file = FMLPaths.CONFIGDIR.get().resolve(FILE);
        if (!Files.exists(file)) {
            return;
        }
        try (CommentedFileConfig config = CommentedFileConfig.builder(file).build()) {
            config.load();
            values = CommentedConfig.copy(config); // keep the values after the file is closed
        } catch (Exception e) {
            BetterSurvival.LOGGER.warn("Could not read {} early, every enchantment will be registered", FILE, e);
        }
    }

    /**
     * False when [enchantments] key (e.g. "educationMaxLevel") is 0 in the config file. A missing file,
     * a missing key or a value that isn't a number counts as on, like the default max levels.
     */
    public static boolean enabled(String maxLevelKey) {
        if (!loaded) {
            load();
        }
        Object value = values == null ? null : values.get(List.of("enchantments", maxLevelKey));
        return !(value instanceof Number number) || number.intValue() > 0;
    }
}
