package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.enchantments.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister
            .create(ForgeRegistries.ENCHANTMENTS, BetterSurvival.MOD_ID);

    public static final RegistryObject<Enchantment> VAMPIRISM = ENCHANTMENTS.register("vampirism",
            VampirismEnchantment::new);
    public static final RegistryObject<Enchantment> EDUCATION = ENCHANTMENTS.register("education",
            EducationEnchantment::new);
    public static final RegistryObject<Enchantment> ASSASSINATE = ENCHANTMENTS.register("assassinate",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.DAGGER));
    public static final RegistryObject<Enchantment> BASH = ENCHANTMENTS.register("bash",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.HAMMER));
    public static final RegistryObject<Enchantment> COMBO = ENCHANTMENTS.register("combo",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.NUNCHAKU));
    public static final RegistryObject<Enchantment> DISARM = ENCHANTMENTS.register("disarm",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.BATTLEAXE));

    public static final RegistryObject<Enchantment> ARROW_RECOVERY = ENCHANTMENTS.register("arrowrecovery",
            ArrowRecoveryEnchantment::new);
    public static final RegistryObject<Enchantment> MULTISHOT = ENCHANTMENTS.register("multishot",
            MultishotEnchantment::new);
    public static final RegistryObject<Enchantment> RAPID_FIRE = ENCHANTMENTS.register("rapidfire",
            RapidFireEnchantment::new);
    public static final RegistryObject<Enchantment> RANGE = ENCHANTMENTS.register("range",
            RangeEnchantment::new);

    public static final RegistryObject<Enchantment> TUNNELING = ENCHANTMENTS.register("tunneling",
            TunnelingEnchantment::new);
    public static final RegistryObject<Enchantment> DIAMONDS = ENCHANTMENTS.register("diamonds",
            DiamondsEnchantment::new);
    public static final RegistryObject<Enchantment> VERSATILITY = ENCHANTMENTS.register("versatility",
            VersatilityEnchantment::new);

    public static final RegistryObject<Enchantment> AGILITY = ENCHANTMENTS.register("agility",
            AgilityEnchantment::new);
    public static final RegistryObject<Enchantment> HIGH_JUMP = ENCHANTMENTS.register("highjump",
            HighJumpEnchantment::new);
    public static final RegistryObject<Enchantment> VITALITY = ENCHANTMENTS.register("vitality",
            VitalityEnchantment::new);
}
