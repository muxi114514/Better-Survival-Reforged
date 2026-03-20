package com.chen1335.ultimateEnchantment;

import com.chen1335.ultimateEnchantment.common.AttributeTypeInfo;
import com.chen1335.ultimateEnchantment.data.LootProvider;
import com.chen1335.ultimateEnchantment.data.UERecipeProvider;
import com.chen1335.ultimateEnchantment.effect.MobEffects;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.enchantment.config.EnchantmentConfig;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.Legend;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IEnchantmentExtension;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(UltimateEnchantment.MODID)
public class UltimateEnchantment {
    public static final String MODID = "ultimate_enchantment";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static boolean canBoosDropEnchantmentBook = true;

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), "ultimate_enchantment");

    public static Configuration config = new Configuration(new File(configDir, "enchantments.cfg"));

    public static Configuration commonConfig = new Configuration(new File(configDir, "common.cfg"));


    public static final RegistryObject<CreativeModeTab> ULTIMATE_ENCHANTMENT_TAB = CREATIVE_MODE_TABS.register("ultimate_enchantment_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(Items.ENCHANTED_BOOK::getDefaultInstance)
            .title(Component.translatable("itemGroup.ultimate_enchantment"))
            .displayItems((parameters, output) -> {
                ArrayList<Enchantment> ultimateEnchantments = new ArrayList<>();
                ArrayList<Enchantment> legendaryEnchantments = new ArrayList<>();
                ArrayList<Enchantment> otherEnchantments = new ArrayList<>();

                Enchantments.ENCHANTMENT_DEFERRED_REGISTER.getEntries().forEach(object -> {
                    Enchantment enchantment = object.get();
                    IEnchantmentExtension enchantmentExtension = (IEnchantmentExtension) enchantment;
                    if (enchantmentExtension.ue$getEnchantmentType() == EnchantmentType.ULTIMATE_ENCHANTMENT) {
                        ultimateEnchantments.add(enchantment);
                    } else if (enchantmentExtension.ue$getEnchantmentType() == EnchantmentType.LEGENDARY_ENCHANTMENT) {
                        legendaryEnchantments.add(enchantment);
                    } else {
                        otherEnchantments.add(enchantment);
                    }
                });

                ItemStack allEnchantment = Items.ENCHANTED_BOOK.getDefaultInstance();

                ultimateEnchantments.forEach(ultimateEnchantment -> {
                    ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), book);
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), allEnchantment);

                    output.accept(book);
                });

                legendaryEnchantments.forEach(ultimateEnchantment -> {
                    ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), book);
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), allEnchantment);
                    output.accept(book);
                });

                otherEnchantments.forEach(ultimateEnchantment -> {
                    ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), book);
                    EnchantmentHelper.setEnchantments(ImmutableMap.of(ultimateEnchantment, ultimateEnchantment.getMaxLevel()), allEnchantment);
                    output.accept(book);
                });

                output.accept(allEnchantment);
            }).build());

    public UltimateEnchantment() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::runData);
        CREATIVE_MODE_TABS.register(modEventBus);
        MobEffects.MOB_EFFECT_DEFERRED_REGISTER.register(modEventBus);
        Enchantments.ENCHANTMENT_DEFERRED_REGISTER.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public void runData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(true, new LootProvider(packOutput, Set.of()));
        event.getGenerator().addProvider(true, new UERecipeProvider(packOutput));
    }


    private void commonSetup(final FMLCommonSetupEvent event) {
        AttributeTypeInfo.init();
        EnchantmentConfig.load(config);
        canBoosDropEnchantmentBook = commonConfig.getBoolean("canBoosDropEnchantmentBook", "common", true, "can Boos Drop Enchantment Book");

        List<String> legendBlackList = List.of(commonConfig.getStringList("legendBlackList", "common", new String[]{ForgeMod.ENTITY_GRAVITY.getId().toString()}, "the attributes that legend will not boost"));

        Legend.buildBlackList(legendBlackList);
        if (config.hasChanged()) {
            config.save();
        }

        if (commonConfig.hasChanged()) {
            commonConfig.save();
        }
    }

    public enum EnchantmentType {
        ULTIMATE_ENCHANTMENT,
        LEGENDARY_ENCHANTMENT,
        OTHER
    }


}
