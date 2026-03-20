package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS,
            BetterSurvival.MOD_ID);

    // --- Blindness (vanilla effect, custom potion type) ---
    public static final RegistryObject<Potion> BLINDNESS = POTIONS.register("blindness",
            () -> new Potion(new MobEffectInstance(MobEffects.BLINDNESS, 300))); // 15s
    public static final RegistryObject<Potion> LONG_BLINDNESS = POTIONS.register("long_blindness",
            () -> new Potion("blindness", new MobEffectInstance(MobEffects.BLINDNESS, 800))); // 40s

    // --- Decay / Wither (vanilla effect, custom potion type) ---
    public static final RegistryObject<Potion> DECAY = POTIONS.register("decay",
            () -> new Potion(new MobEffectInstance(MobEffects.WITHER, 800))); // 40s
    public static final RegistryObject<Potion> LONG_DECAY = POTIONS.register("long_decay",
            () -> new Potion("decay", new MobEffectInstance(MobEffects.WITHER, 2134))); // 1:46
    public static final RegistryObject<Potion> STRONG_DECAY = POTIONS.register("strong_decay",
            () -> new Potion("decay", new MobEffectInstance(MobEffects.WITHER, 400, 1))); // 20s, II

    // --- Warp (custom instant teleport) ---
    public static final RegistryObject<Potion> WARP = POTIONS.register("warp",
            () -> new Potion(new MobEffectInstance(ModMobEffects.WARP.get(), 1)));
    public static final RegistryObject<Potion> STRONG_WARP = POTIONS.register("strong_warp",
            () -> new Potion("warp", new MobEffectInstance(ModMobEffects.WARP.get(), 1, 1)));

    // --- Antiwarp (prevents Enderman teleportation) ---
    public static final RegistryObject<Potion> ANTIWARP = POTIONS.register("antiwarp",
            () -> new Potion(new MobEffectInstance(ModMobEffects.ANTIWARP.get(), 1800))); // 1:30
    public static final RegistryObject<Potion> LONG_ANTIWARP = POTIONS.register("long_antiwarp",
            () -> new Potion("antiwarp", new MobEffectInstance(ModMobEffects.ANTIWARP.get(), 4800))); // 4:00

    // --- Milk (cure all) ---
    public static final RegistryObject<Potion> MILK = POTIONS.register("milk",
            () -> new Potion(new MobEffectInstance(ModMobEffects.MILK.get(), 1)));

    // --- Cure (remove bad effects) ---
    public static final RegistryObject<Potion> CURE = POTIONS.register("cure",
            () -> new Potion(new MobEffectInstance(ModMobEffects.CURE.get(), 1)));

    // --- Dispel (remove good effects) ---
    public static final RegistryObject<Potion> DISPEL = POTIONS.register("dispel",
            () -> new Potion(new MobEffectInstance(ModMobEffects.DISPEL.get(), 1)));
}
