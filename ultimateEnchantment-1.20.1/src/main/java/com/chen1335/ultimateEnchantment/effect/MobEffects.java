package com.chen1335.ultimateEnchantment.effect;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.awt.*;

public class MobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, UltimateEnchantment.MODID);

    public static final RegistryObject<UnActiveVanquisher> UN_ACTIVE_VANQUISHER = MOB_EFFECT_DEFERRED_REGISTER.register("un_active_vanquisher", () -> new UnActiveVanquisher(MobEffectCategory.BENEFICIAL, new Color(255, 255, 255, 0).getRGB()));

    public static final RegistryObject<ActiveVanquisher> ACTIVE_VANQUISHER = MOB_EFFECT_DEFERRED_REGISTER.register("active_vanquisher", () -> new ActiveVanquisher(MobEffectCategory.BENEFICIAL, new Color(255, 255, 255, 0).getRGB()));

}
