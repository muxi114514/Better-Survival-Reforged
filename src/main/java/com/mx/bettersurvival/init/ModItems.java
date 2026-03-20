package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import com.mx.bettersurvival.integration.IaFCompat;

public class ModItems {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
                        BetterSurvival.MOD_ID);

        // ==================== Tier Definitions ====================

        /** Pairs of (Tier, lowercaseName) for registration loops. */
        private record TierEntry(Tier tier, String name) {
        }

        private static final List<TierEntry> TIERS = List.of(
                        new TierEntry(Tiers.WOOD, "wood"),
                        new TierEntry(Tiers.STONE, "stone"),
                        new TierEntry(Tiers.IRON, "iron"),
                        new TierEntry(Tiers.GOLD, "gold"),
                        new TierEntry(Tiers.DIAMOND, "diamond"),
                        new TierEntry(ModTiers.EMERALD, "emerald"),
                        new TierEntry(ModTiers.CRYING_OBSIDIAN, "cryingobsidian"));

        // ==================== Default weapon stats (match ModConfig defaults)
        // ====================
        // Config values cannot be read at registration time, so we use hardcoded
        // defaults.
        // Config-driven attribute overrides can be implemented via events in a future
        // phase.

        private static final float HAMMER_DMG = 1.2F, HAMMER_SPD = 1.35F;
        private static final float SPEAR_DMG = 0.75F, SPEAR_SPD = 1.0F, SPEAR_REACH = 2.0F;
        private static final float DAGGER_DMG = 0.7F, DAGGER_SPD = 0.8F;
        private static final float BATTLEAXE_DMG = 1.6F, BATTLEAXE_SPD = 1.25F;
        private static final float NUNCHAKU_DMG = 0.5F, NUNCHAKU_SPD = 0.3F;

        // ==================== Weapon Registration ====================

        // Store all weapon registry objects for creative tab population
        public static final List<RegistryObject<Item>> ALL_WEAPONS = new ArrayList<>();

        static {
                // Register vanilla tier weapons
                for (TierEntry entry : TIERS) {
                        registerWeaponsForTier(entry.tier(), entry.name());
                }

                // Register IaF CE tier weapons (if IaF is present)
                boolean iafPresent = net.minecraftforge.fml.ModList.get().isLoaded("iceandfire");

                if (iafPresent) {
                        for (IaFCompat.IafTierEntry iafEntry : IaFCompat.getIafTierEntries()) {
                                registerWeaponsForTier(iafEntry.tier(), iafEntry.name());
                        }
                }

                // Register Defiled Lands tier weapons (if Defiled Lands is present)
                boolean defiledPresent = net.minecraftforge.fml.ModList.get().isLoaded("defiledlands");

                if (defiledPresent) {
                        for (com.mx.bettersurvival.integration.DefiledCompat.DefiledTierEntry dEntry : com.mx.bettersurvival.integration.DefiledCompat
                                        .getDefiledTierEntries()) {
                                registerWeaponsForTier(dEntry.tier(), dEntry.name());
                        }
                }
        }

        private static void registerWeaponsForTier(Tier tier, String name) {
                ALL_WEAPONS.add(ITEMS.register("item" + name + "hammer",
                                () -> new HammerItem(tier, HAMMER_DMG, HAMMER_SPD, new Item.Properties())));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "spear",
                                () -> new SpearItem(tier, SPEAR_DMG, SPEAR_SPD, SPEAR_REACH, new Item.Properties())));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "dagger",
                                () -> new DaggerItem(tier, DAGGER_DMG, DAGGER_SPD, new Item.Properties())));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "battleaxe",
                                () -> new BattleAxeItem(tier, BATTLEAXE_DMG, BATTLEAXE_SPD, new Item.Properties())));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "nunchaku",
                                () -> new NunchakuItem(tier, NUNCHAKU_DMG, NUNCHAKU_SPD, new Item.Properties())));
        }
}
