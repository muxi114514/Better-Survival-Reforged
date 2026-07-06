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

        private record TierEntry(Tier tier, String name) {
        }

        private static final List<TierEntry> TIERS = List.of(
                        new TierEntry(Tiers.WOOD, "wood"),
                        new TierEntry(Tiers.STONE, "stone"),
                        new TierEntry(Tiers.IRON, "iron"),
                        new TierEntry(Tiers.GOLD, "gold"),
                        new TierEntry(Tiers.DIAMOND, "diamond"),
                        new TierEntry(ModTiers.EMERALD, "emerald"),
                        new TierEntry(ModTiers.CRYING_OBSIDIAN, "cryingobsidian"),
                        new TierEntry(ModTiers.NETHERITE, "netherite"),
                        // SRP 风味（感知/觉知）：常驻、无配方，特效接口见 SrpCompat
                        new TierEntry(ModTiers.LIVING, "living"),
                        new TierEntry(ModTiers.SENTIENT, "sentient"));

        private static final float HAMMER_DMG = 1.2F, HAMMER_SPD = 1.35F;
        private static final float SPEAR_DMG = 0.75F, SPEAR_SPD = 1.0F, SPEAR_REACH = 2.0F;
        private static final float DAGGER_DMG = 0.7F, DAGGER_SPD = 0.8F;
        private static final float BATTLEAXE_DMG = 1.6F, BATTLEAXE_SPD = 1.25F;
        private static final float NUNCHAKU_DMG = 0.5F, NUNCHAKU_SPD = 0.3F;

        public static final List<RegistryObject<Item>> ALL_WEAPONS = new ArrayList<>();

        // 盾牌（基础物品，无模组依赖）：小盾 blockPower 0.5/weight 1，大盾 0.8/weight 3
        public static final RegistryObject<Item> SMALL_SHIELD = ITEMS.register("itemsmallshield",
                        () -> new CustomShieldItem(0.5F, 1, new Item.Properties()));
        public static final RegistryObject<Item> BIG_SHIELD = ITEMS.register("itembigshield",
                        () -> new CustomShieldItem(0.8F, 3, new Item.Properties()));

        static {

                for (TierEntry entry : TIERS) {
                        registerWeaponsForTier(entry.tier(), entry.name());
                }

                boolean iafPresent = net.minecraftforge.fml.ModList.get().isLoaded("iceandfire");

                if (iafPresent) {
                        for (IaFCompat.IafTierEntry iafEntry : IaFCompat.getIafTierEntries()) {
                                registerWeaponsForTier(iafEntry.tier(), iafEntry.name());
                        }
                }

                boolean defiledPresent = net.minecraftforge.fml.ModList.get().isLoaded("defiledlands");

                if (defiledPresent) {
                        for (com.mx.bettersurvival.integration.DefiledCompat.DefiledTierEntry dEntry : com.mx.bettersurvival.integration.DefiledCompat
                                        .getDefiledTierEntries()) {
                                registerWeaponsForTier(dEntry.tier(), dEntry.name());
                        }
                }

                boolean enigmaticPresent = net.minecraftforge.fml.ModList.get().isLoaded("enigmaticlegacy");

                if (enigmaticPresent) {
                        for (com.mx.bettersurvival.integration.EnigmaticCompat.EnigmaticTierEntry eEntry : com.mx.bettersurvival.integration.EnigmaticCompat
                                        .getEnigmaticTierEntries()) {
                                registerWeaponsForTier(eEntry.tier(), eEntry.name());
                        }
                }

                boolean twilightPresent = net.minecraftforge.fml.ModList.get().isLoaded("twilightforest");

                if (twilightPresent) {
                        for (com.mx.bettersurvival.integration.TwilightCompat.TwilightTierEntry tEntry : com.mx.bettersurvival.integration.TwilightCompat
                                        .getTwilightTierEntries()) {
                                // 赤铁武器防火（掉岩浆不烧毁）
                                registerWeaponsForTier(tEntry.tier(), tEntry.name(),
                                                com.mx.bettersurvival.integration.TwilightCompat.isFieryTier(tEntry.tier()));
                        }
                }

                boolean cataclysmPresent = net.minecraftforge.fml.ModList.get().isLoaded("cataclysm");

                if (cataclysmPresent) {
                        for (com.mx.bettersurvival.integration.CataclysmCompat.CataclysmTierEntry cEntry : com.mx.bettersurvival.integration.CataclysmCompat
                                        .getCataclysmTierEntries()) {
                                // 腾炎/咒魂/凋零 三招牌材料防火
                                registerWeaponsForTier(cEntry.tier(), cEntry.name(),
                                                com.mx.bettersurvival.integration.CataclysmCompat.isFireproofTier(cEntry.tier()));
                        }
                }
        }

        private static void registerWeaponsForTier(Tier tier, String name) {
                registerWeaponsForTier(tier, name, false);
        }

        private static void registerWeaponsForTier(Tier tier, String name, boolean fireResistant) {
                ALL_WEAPONS.add(ITEMS.register("item" + name + "hammer",
                                () -> new HammerItem(tier, HAMMER_DMG, HAMMER_SPD, props(fireResistant))));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "spear",
                                () -> new SpearItem(tier, SPEAR_DMG, SPEAR_SPD, SPEAR_REACH, props(fireResistant))));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "dagger",
                                () -> new DaggerItem(tier, DAGGER_DMG, DAGGER_SPD, props(fireResistant))));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "battleaxe",
                                () -> new BattleAxeItem(tier, BATTLEAXE_DMG, BATTLEAXE_SPD, props(fireResistant))));
                ALL_WEAPONS.add(ITEMS.register("item" + name + "nunchaku",
                                () -> new NunchakuItem(tier, NUNCHAKU_DMG, NUNCHAKU_SPD, props(fireResistant))));
        }

        private static Item.Properties props(boolean fireResistant) {
                Item.Properties p = new Item.Properties();
                return fireResistant ? p.fireResistant() : p;
        }
}
