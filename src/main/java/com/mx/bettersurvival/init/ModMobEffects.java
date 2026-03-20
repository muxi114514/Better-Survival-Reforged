package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.effects.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,
            BetterSurvival.MOD_ID);

    public static final RegistryObject<MobEffect> STUN = MOB_EFFECTS.register("stun", StunEffect::new);
    public static final RegistryObject<MobEffect> WARP = MOB_EFFECTS.register("warp", WarpEffect::new);
    public static final RegistryObject<MobEffect> ANTIWARP = MOB_EFFECTS.register("antiwarp", AntiwarpEffect::new);
    public static final RegistryObject<MobEffect> MILK = MOB_EFFECTS.register("milk", MilkEffect::new);
    public static final RegistryObject<MobEffect> CURE = MOB_EFFECTS.register("cure",
            () -> new CleanseEffect(MobEffectCategory.BENEFICIAL, 0xFFE34D)); // golden
    public static final RegistryObject<MobEffect> DISPEL = MOB_EFFECTS.register("dispel",
            () -> new CleanseEffect(MobEffectCategory.HARMFUL, 0xB5E8FF)); // light blue
}
