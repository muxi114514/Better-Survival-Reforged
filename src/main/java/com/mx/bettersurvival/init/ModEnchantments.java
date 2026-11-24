package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.enchantments.*;
import com.mx.bettersurvival.config.EarlyEnchantmentConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister
            .create(ForgeRegistries.ENCHANTMENTS, BetterSurvival.MOD_ID);

    public static final RegistryObject<Enchantment> VAMPIRISM = register("vampirism", "vampirismMaxLevel",
            VampirismEnchantment::new);
    public static final RegistryObject<Enchantment> EDUCATION = register("education", "educationMaxLevel",
            EducationEnchantment::new);
    public static final RegistryObject<Enchantment> ASSASSINATE = register("assassinate", "assassinateMaxLevel",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.DAGGER));
    public static final RegistryObject<Enchantment> BASH = register("bash", "bashMaxLevel",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.HAMMER));
    public static final RegistryObject<Enchantment> COMBO = register("combo", "comboMaxLevel",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.NUNCHAKU));
    public static final RegistryObject<Enchantment> DISARM = register("disarm", "disarmMaxLevel",
            () -> new WeaponSpecialEnchantment(WeaponSpecialEnchantment.WeaponType.BATTLEAXE));

    public static final RegistryObject<Enchantment> ARROW_RECOVERY = register("arrowrecovery", "arrowRecoveryMaxLevel",
            ArrowRecoveryEnchantment::new);
    public static final RegistryObject<Enchantment> MULTISHOT = register("multishot", "multishotMaxLevel",
            MultishotEnchantment::new);
    public static final RegistryObject<Enchantment> RAPID_FIRE = register("rapidfire", "rapidFireMaxLevel",
            RapidFireEnchantment::new);
    public static final RegistryObject<Enchantment> RANGE = register("range", "rangeMaxLevel",
            RangeEnchantment::new);

    public static final RegistryObject<Enchantment> TUNNELING = register("tunneling", "tunnelingMaxLevel",
            TunnelingEnchantment::new);
    public static final RegistryObject<Enchantment> DIAMONDS = register("diamonds", "diamondsEverywhereMaxLevel",
            DiamondsEnchantment::new);
    public static final RegistryObject<Enchantment> VERSATILITY = register("versatility", "versatilityMaxLevel",
            VersatilityEnchantment::new);

    public static final RegistryObject<Enchantment> AGILITY = register("agility", "agilityMaxLevel",
            AgilityEnchantment::new);
    public static final RegistryObject<Enchantment> HIGH_JUMP = register("highjump", "highJumpMaxLevel",
            HighJumpEnchantment::new);
    public static final RegistryObject<Enchantment> VITALITY = register("vitality", "vitalityMaxLevel",
            VitalityEnchantment::new);

    // 盾牌附魔
    public static final RegistryObject<Enchantment> BLOCK_POWER = register("blockpower", "blockingPowerMaxLevel",
            BlockPowerEnchantment::new);
    public static final RegistryObject<Enchantment> SPELL_SHIELD = register("spellshield", "spellShieldMaxLevel",
            SpellShieldEnchantment::new);
    public static final RegistryObject<Enchantment> REFLECTION = register("reflection", "reflectionMaxLevel",
            ReflectionEnchantment::new);
    public static final RegistryObject<Enchantment> HEAVY = register("heavy", "heavyMaxLevel",
            HeavyEnchantment::new);
    public static final RegistryObject<Enchantment> WEIGHTLESS = register("weightless", "weightlessMaxLevel",
            WeightlessEnchantment::new);

    /**
     * Registers the enchantment unless its max level is 0 in the config file. A turned-off enchantment
     * gets a RegistryObject that is never present, so it can't come from anywhere (loot, villager trades,
     * the enchanting table, commands); before, a max level of 0 only kept it off books and loot or trades
     * still gave level 1 of it. Changing a max level to or from 0 needs a game restart.
     */
    private static RegistryObject<Enchantment> register(String name, String maxLevelKey, Supplier<Enchantment> enchantment) {
        if (EarlyEnchantmentConfig.enabled(maxLevelKey)) {
            return ENCHANTMENTS.register(name, enchantment);
        }
        BetterSurvival.LOGGER.info("Enchantment {}:{} is turned off ({} = 0), not registering it", BetterSurvival.MOD_ID, name, maxLevelKey);
        return RegistryObject.create(new ResourceLocation(BetterSurvival.MOD_ID, name), ForgeRegistries.ENCHANTMENTS);
    }

    /** Level of the enchantment on the stack, 0 when it's turned off (not registered) or there's no stack. */
    public static int level(RegistryObject<Enchantment> enchantment, ItemStack stack) {
        return enchantment.isPresent() && stack != null ? stack.getEnchantmentLevel(enchantment.get()) : 0;
    }

    /** Highest level of the enchantment on what the entity has equipped, 0 when it's turned off. */
    public static int level(RegistryObject<Enchantment> enchantment, LivingEntity entity) {
        return enchantment.isPresent() ? EnchantmentHelper.getEnchantmentLevel(enchantment.get(), entity) : 0;
    }

    /** Whether other is this enchantment (never true for a turned-off one). */
    public static boolean is(RegistryObject<Enchantment> enchantment, Enchantment other) {
        return enchantment.isPresent() && enchantment.get() == other;
    }
}
