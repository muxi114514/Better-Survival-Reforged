package com.chen1335.ultimateEnchantment.enchantment;

import com.chen1335.ultimateEnchantment.UltimateEnchantment;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.Terminator;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class Enchantments {
    public static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static final DeferredRegister<Enchantment> ENCHANTMENT_DEFERRED_REGISTER = DeferredRegister.create(Registries.ENCHANTMENT, UltimateEnchantment.MODID);

    public static final RegistryObject<Ultimate> ULTIMATE = ENCHANTMENT_DEFERRED_REGISTER.register("ultimate", Ultimate::new);

    public static final RegistryObject<Legend> LEGEND = ENCHANTMENT_DEFERRED_REGISTER.register("legend", Legend::new);

    public static final RegistryObject<LastStand> LAST_STAND = ENCHANTMENT_DEFERRED_REGISTER.register("last_stand", LastStand::new);

    public static final RegistryObject<Smelting> SMELTING = ENCHANTMENT_DEFERRED_REGISTER.register("smelting", Smelting::new);

    public static final RegistryObject<CriticalChance> CRITICAL_CHANCE = ENCHANTMENT_DEFERRED_REGISTER.register("critical_chance", CriticalChance::new);

    public static final RegistryObject<CriticalDamage> CRITICAL_DAMAGE = ENCHANTMENT_DEFERRED_REGISTER.register("critical_damage", CriticalDamage::new);

    public static final RegistryObject<Enchantment> QUICK_LATCH = ENCHANTMENT_DEFERRED_REGISTER.register("quick_latch", () -> new Enchantment(Enchantment.Rarity.COMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
        @Override
        public int getMaxCost(int pLevel) {
            return 60;
        }

        @Override
        public int getMinCost(int pLevel) {
            return 10;
        }
    });

    public static final RegistryObject<PierceThrough> PIERCE_THROUGH = ENCHANTMENT_DEFERRED_REGISTER.register("pierce_through", PierceThrough::new);

    public static final RegistryObject<Scabbing> SCABBING = ENCHANTMENT_DEFERRED_REGISTER.register("scabbing", Scabbing::new);

    public static final RegistryObject<LifeSteal> LIFE_STEAL = ENCHANTMENT_DEFERRED_REGISTER.register("life_steal", LifeSteal::new);

    public static final RegistryObject<CutDown> CUT_DOWN = ENCHANTMENT_DEFERRED_REGISTER.register("cut_down", CutDown::new);

    public static final RegistryObject<QuickShooting> QUICK_SHOOTING = ENCHANTMENT_DEFERRED_REGISTER.register("quick_shooting", QuickShooting::new);

    public static final RegistryObject<Terminator> TERMINATOR = ENCHANTMENT_DEFERRED_REGISTER.register("terminator", Terminator::new);

    public static final RegistryObject<Vanquisher> VANQUISHER = ENCHANTMENT_DEFERRED_REGISTER.register("vanquisher", Vanquisher::new);

    public static final RegistryObject<Eternal> ETERNAL = ENCHANTMENT_DEFERRED_REGISTER.register("eternal", Eternal::new);

}
