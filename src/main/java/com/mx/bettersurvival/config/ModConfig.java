package com.mx.bettersurvival.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {

        public static final ForgeConfigSpec COMMON_SPEC;
        public static final CommonConfig COMMON;

        public static final ForgeConfigSpec CLIENT_SPEC;
        public static final ClientConfig CLIENT;

        static {
                final Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                                .configure(CommonConfig::new);
                COMMON_SPEC = commonPair.getRight();
                COMMON = commonPair.getLeft();

                final Pair<ClientConfig, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder()
                                .configure(ClientConfig::new);
                CLIENT_SPEC = clientPair.getRight();
                CLIENT = clientPair.getLeft();
        }

        // =========================================================================
        // Common Config (synced server → client)
        // =========================================================================
        public static class CommonConfig {
                // --- Weapon ---
                public final ForgeConfigSpec.DoubleValue spearReachBonus;
                public final ForgeConfigSpec.DoubleValue stunBaseChance;
                public final ForgeConfigSpec.DoubleValue bashModifier;
                public final ForgeConfigSpec.DoubleValue disarmBaseChance;
                public final ForgeConfigSpec.DoubleValue disarmModifier;
                public final ForgeConfigSpec.DoubleValue battleAxeDmgMod;
                public final ForgeConfigSpec.DoubleValue nunchakuDmgMod;
                public final ForgeConfigSpec.DoubleValue hammerDmgMod;
                public final ForgeConfigSpec.DoubleValue daggerDmgMod;
                public final ForgeConfigSpec.DoubleValue spearDmgMod;
                public final ForgeConfigSpec.DoubleValue crossbowDmgMod;
                public final ForgeConfigSpec.DoubleValue battleAxeSpd;
                public final ForgeConfigSpec.DoubleValue nunchakuSpd;
                public final ForgeConfigSpec.DoubleValue hammerSpd;
                public final ForgeConfigSpec.DoubleValue daggerSpd;
                public final ForgeConfigSpec.DoubleValue spearSpd;
                public final ForgeConfigSpec.IntValue crossbowReloadTime;
                public final ForgeConfigSpec.BooleanValue allowVanillaShields;

                // --- Enchantment max levels ---
                public final ForgeConfigSpec.IntValue assassinateLevel;
                public final ForgeConfigSpec.IntValue agilityLevel;
                public final ForgeConfigSpec.IntValue arrowRecoveryLevel;
                public final ForgeConfigSpec.IntValue bashLevel;
                public final ForgeConfigSpec.IntValue blastLevel;
                public final ForgeConfigSpec.IntValue blockingPowerLevel;
                public final ForgeConfigSpec.IntValue comboLevel;
                public final ForgeConfigSpec.IntValue diamondsEverywhereLevel;
                public final ForgeConfigSpec.IntValue disarmLevel;
                public final ForgeConfigSpec.IntValue educationLevel;
                public final ForgeConfigSpec.IntValue flingLevel;
                public final ForgeConfigSpec.IntValue heavyLevel;
                public final ForgeConfigSpec.IntValue highJumpLevel;
                public final ForgeConfigSpec.IntValue multishotLevel;
                public final ForgeConfigSpec.IntValue penetrationLevel;
                public final ForgeConfigSpec.IntValue rangeLevel;
                public final ForgeConfigSpec.IntValue rapidFireLevel;
                public final ForgeConfigSpec.IntValue reflectionLevel;
                public final ForgeConfigSpec.IntValue smeltingLevel;
                public final ForgeConfigSpec.IntValue spellShieldLevel;
                public final ForgeConfigSpec.IntValue tunnelingLevel;
                public final ForgeConfigSpec.IntValue vampirismLevel;
                public final ForgeConfigSpec.IntValue versatilityLevel;
                public final ForgeConfigSpec.IntValue vitalityLevel;
                public final ForgeConfigSpec.IntValue weightlessLevel;

                // --- Enchantment treasure flags ---
                public final ForgeConfigSpec.BooleanValue assassinateTreasure;
                public final ForgeConfigSpec.BooleanValue agilityTreasure;
                public final ForgeConfigSpec.BooleanValue arrowRecoveryTreasure;
                public final ForgeConfigSpec.BooleanValue bashTreasure;
                public final ForgeConfigSpec.BooleanValue blastTreasure;
                public final ForgeConfigSpec.BooleanValue blockingPowerTreasure;
                public final ForgeConfigSpec.BooleanValue comboTreasure;
                public final ForgeConfigSpec.BooleanValue diamondsEverywhereTreasure;
                public final ForgeConfigSpec.BooleanValue disarmTreasure;
                public final ForgeConfigSpec.BooleanValue educationTreasure;
                public final ForgeConfigSpec.BooleanValue flingTreasure;
                public final ForgeConfigSpec.BooleanValue heavyTreasure;
                public final ForgeConfigSpec.BooleanValue highJumpTreasure;
                public final ForgeConfigSpec.BooleanValue multishotTreasure;
                public final ForgeConfigSpec.BooleanValue penetrationTreasure;
                public final ForgeConfigSpec.BooleanValue rangeTreasure;
                public final ForgeConfigSpec.BooleanValue rapidFireTreasure;
                public final ForgeConfigSpec.BooleanValue reflectionTreasure;
                public final ForgeConfigSpec.BooleanValue smeltingTreasure;
                public final ForgeConfigSpec.BooleanValue spellShieldTreasure;
                public final ForgeConfigSpec.BooleanValue tunnelingTreasure;
                public final ForgeConfigSpec.BooleanValue vampirismTreasure;
                public final ForgeConfigSpec.BooleanValue versatilityTreasure;
                public final ForgeConfigSpec.BooleanValue vitalityTreasure;
                public final ForgeConfigSpec.BooleanValue weightlessTreasure;

                // --- Enchantment misc ---
                public final ForgeConfigSpec.DoubleValue rangeVelocity;
                public final ForgeConfigSpec.BooleanValue preventTunnelingBlockEntities;

                // --- Potion Coating ---
                public final ForgeConfigSpec.IntValue potionHitsBase;
                public final ForgeConfigSpec.IntValue potionHitsMax;
                public final ForgeConfigSpec.DoubleValue potionDurationDivisor;
                public final ForgeConfigSpec.IntValue potionAmplifierModifier;
                public final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> potionBlacklist;

                // --- Materials ---
                public final ForgeConfigSpec.BooleanValue moddedMaterials;

                CommonConfig(ForgeConfigSpec.Builder builder) {
                        // ---- Weapons ----
                        builder.comment("Weapon settings").push("weapons");

                        spearReachBonus = builder.comment("Extra attack reach given to spears.")
                                        .defineInRange("spearReachBonus", 2.0, 0.0, 10.0);
                        stunBaseChance = builder
                                        .comment("Base chance that a full-strength hammer attack stuns the target.")
                                        .defineInRange("stunBaseChance", 0.1, 0.0, 1.0);
                        bashModifier = builder.comment("Increased stun chance per level of the Bash enchantment.")
                                        .defineInRange("bashModifier", 0.05, 0.0, 1.0);
                        disarmBaseChance = builder.comment(
                                        "Base chance that a full-strength battle axe attack causes the target to drop their weapon.")
                                        .defineInRange("disarmBaseChance", 0.1, 0.0, 1.0);
                        disarmModifier = builder
                                        .comment("Disarm chance increase per level of the Disarming enchantment.")
                                        .defineInRange("disarmModifier", 0.05, 0.0, 1.0);

                        battleAxeDmgMod = builder
                                        .comment("Damage factor for battle axes (compared to a sword of the same material).")
                                        .defineInRange("battleAxeDmgMod", 1.6, 0.1, 10.0);
                        nunchakuDmgMod = builder.comment("Damage factor for nunchaku.")
                                        .defineInRange("nunchakuDmgMod", 0.5, 0.1, 10.0);
                        hammerDmgMod = builder.comment("Damage factor for hammers.")
                                        .defineInRange("hammerDmgMod", 1.2, 0.1, 10.0);
                        daggerDmgMod = builder.comment("Damage factor for daggers.")
                                        .defineInRange("daggerDmgMod", 0.7, 0.1, 10.0);
                        spearDmgMod = builder.comment("Damage factor for spears.")
                                        .defineInRange("spearDmgMod", 0.75, 0.1, 10.0);
                        crossbowDmgMod = builder.comment("Damage factor for crossbows.")
                                        .defineInRange("crossbowDmgMod", 2.0, 0.1, 10.0);

                        battleAxeSpd = builder.comment("Attack delay factor for battle axes.")
                                        .defineInRange("battleAxeSpd", 1.25, 0.1, 10.0);
                        nunchakuSpd = builder.comment("Attack delay factor for nunchaku.")
                                        .defineInRange("nunchakuSpd", 0.3, 0.1, 10.0);
                        hammerSpd = builder.comment("Attack delay factor for hammers.")
                                        .defineInRange("hammerSpd", 1.35, 0.1, 10.0);
                        daggerSpd = builder.comment("Attack delay factor for daggers.")
                                        .defineInRange("daggerSpd", 0.8, 0.1, 10.0);
                        spearSpd = builder.comment("Attack delay factor for spears.")
                                        .defineInRange("spearSpd", 1.0, 0.1, 10.0);

                        crossbowReloadTime = builder.comment("The time it takes to load the crossbow (in ticks).")
                                        .defineInRange("crossbowReloadTime", 40, 1, 200);
                        allowVanillaShields = builder.comment("When set to false, disables Vanilla shield recipe.")
                                        .define("allowVanillaShields", true);

                        builder.pop();

                        // ---- Enchantments ----
                        builder.comment("Enchantment settings").push("enchantments");

                        assassinateLevel = builder.defineInRange("assassinateMaxLevel", 3, 0, 10);
                        agilityLevel = builder.defineInRange("agilityMaxLevel", 2, 0, 10);
                        arrowRecoveryLevel = builder.defineInRange("arrowRecoveryMaxLevel", 3, 0, 10);
                        bashLevel = builder.defineInRange("bashMaxLevel", 3, 0, 10);
                        blastLevel = builder.defineInRange("blastMaxLevel", 2, 0, 10);
                        blockingPowerLevel = builder.defineInRange("blockingPowerMaxLevel", 3, 0, 10);
                        comboLevel = builder.defineInRange("comboMaxLevel", 3, 0, 10);
                        diamondsEverywhereLevel = builder.defineInRange("diamondsEverywhereMaxLevel", 3, 0, 10);
                        disarmLevel = builder.defineInRange("disarmMaxLevel", 1, 0, 10);
                        educationLevel = builder.defineInRange("educationMaxLevel", 3, 0, 10);
                        flingLevel = builder.defineInRange("flingMaxLevel", 2, 0, 10);
                        heavyLevel = builder.defineInRange("heavyMaxLevel", 1, 0, 10);
                        highJumpLevel = builder.defineInRange("highJumpMaxLevel", 2, 0, 10);
                        multishotLevel = builder.defineInRange("multishotMaxLevel", 3, 0, 10);
                        penetrationLevel = builder.defineInRange("penetrationMaxLevel", 5, 0, 10);
                        rangeLevel = builder.defineInRange("rangeMaxLevel", 1, 0, 10);
                        rapidFireLevel = builder.defineInRange("rapidFireMaxLevel", 5, 0, 10);
                        reflectionLevel = builder.defineInRange("reflectionMaxLevel", 3, 0, 10);
                        smeltingLevel = builder.defineInRange("smeltingMaxLevel", 1, 0, 10);
                        spellShieldLevel = builder.defineInRange("spellShieldMaxLevel", 3, 0, 10);
                        tunnelingLevel = builder.defineInRange("tunnelingMaxLevel", 2, 0, 10);
                        vampirismLevel = builder.defineInRange("vampirismMaxLevel", 1, 0, 10);
                        versatilityLevel = builder.defineInRange("versatilityMaxLevel", 1, 0, 10);
                        vitalityLevel = builder.defineInRange("vitalityMaxLevel", 1, 0, 10);
                        weightlessLevel = builder.defineInRange("weightlessMaxLevel", 1, 0, 10);

                        assassinateTreasure = builder.define("assassinateTreasure", false);
                        agilityTreasure = builder.define("agilityTreasure", false);
                        arrowRecoveryTreasure = builder.define("arrowRecoveryTreasure", false);
                        bashTreasure = builder.define("bashTreasure", false);
                        blastTreasure = builder.define("blastTreasure", false);
                        blockingPowerTreasure = builder.define("blockingPowerTreasure", false);
                        comboTreasure = builder.define("comboTreasure", false);
                        diamondsEverywhereTreasure = builder.define("diamondsEverywhereTreasure", false);
                        disarmTreasure = builder.define("disarmTreasure", false);
                        educationTreasure = builder.define("educationTreasure", false);
                        flingTreasure = builder.define("flingTreasure", false);
                        heavyTreasure = builder.define("heavyTreasure", false);
                        highJumpTreasure = builder.define("highJumpTreasure", false);
                        multishotTreasure = builder.define("multishotTreasure", false);
                        penetrationTreasure = builder.define("penetrationTreasure", false);
                        rangeTreasure = builder.define("rangeTreasure", false);
                        rapidFireTreasure = builder.define("rapidFireTreasure", false);
                        reflectionTreasure = builder.define("reflectionTreasure", false);
                        smeltingTreasure = builder.define("smeltingTreasure", false);
                        spellShieldTreasure = builder.define("spellShieldTreasure", false);
                        tunnelingTreasure = builder.define("tunnelingTreasure", false);
                        vampirismTreasure = builder.define("vampirismTreasure", false);
                        versatilityTreasure = builder.define("versatilityTreasure", false);
                        vitalityTreasure = builder.define("vitalityTreasure", false);
                        weightlessTreasure = builder.define("weightlessTreasure", false);

                        rangeVelocity = builder.comment("Multiplier of arrow velocity for the Range enchantment.")
                                        .defineInRange("rangeVelocity", 1.5, 1.0, 10.0);
                        preventTunnelingBlockEntities = builder
                                        .comment("Prevent Tunneling from attempting to mine BlockEntities.")
                                        .define("preventTunnelingBlockEntities", true);

                        builder.pop();

                        // ---- Potion Coating ----
                        builder.comment("Potion Coating settings").push("potion_coating");

                        potionHitsBase = builder.comment(
                                        "Base number of charges (hits) added when dipping a weapon in a cauldron.")
                                        .defineInRange("potionHitsBase", 10, 1, 1000);
                        potionHitsMax = builder.comment("Maximum number of charges (hits) a weapon can stack.")
                                        .defineInRange("potionHitsMax", 50, 1, 1000);
                        potionDurationDivisor = builder.comment(
                                        "Divisor for potion duration on hit. Example: 8.0 means effect lasts 1/8 of the original potion time.")
                                        .defineInRange("potionDurationDivisor", 8.0, 1.0, 100.0);
                        potionAmplifierModifier = builder.comment(
                                        "Modifier added to the potion's amplifier level. Use negative numbers to weaken the effect (e.g. -1 turns Poison II into Poison I).")
                                        .defineInRange("potionAmplifierModifier", 0, -10, 10);
                        potionBlacklist = builder
                                        .comment("List of potion registry names that cannot be applied to weapons.")
                                        .defineListAllowEmpty("potionBlacklist", () -> java.util.List
                                                        .of("minecraft:instant_health", "minecraft:healing"),
                                                        o -> o instanceof String);

                        builder.pop();

                        // ---- Materials ----
                        builder.comment("Material settings").push("materials");

                        moddedMaterials = builder
                                        .comment("Allows you to craft weapons from materials commonly present in mods.")
                                        .define("moddedMaterials", true);

                        builder.pop();
                }
        }

        // =========================================================================
        // Client Config
        // =========================================================================
        public static class ClientConfig {
                public final ForgeConfigSpec.BooleanValue fovShields;
                public final ForgeConfigSpec.BooleanValue staticFOV;

                ClientConfig(ForgeConfigSpec.Builder builder) {
                        builder.comment("Client settings").push("client");

                        fovShields = builder.comment("Prevent custom shields from changing FoV.")
                                        .define("fovShields", true);
                        staticFOV = builder.comment("Prevent FoV changes completely.")
                                        .define("staticFOV", false);

                        builder.pop();
                }
        }
}
