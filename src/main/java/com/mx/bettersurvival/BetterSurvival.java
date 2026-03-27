package com.mx.bettersurvival;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.*;
import com.mx.bettersurvival.network.ModNetwork;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(BetterSurvival.MOD_ID)
public class BetterSurvival {
        public static final String MOD_ID = "bettersurvival";
        public static final Logger LOGGER = LogUtils.getLogger();

        public static boolean isIafLoaded;
        public static boolean isDefiledLoaded;
        public static boolean isJMixinLoaded;

        public BetterSurvival() {

                isIafLoaded = net.minecraftforge.fml.ModList.get().isLoaded("iceandfire");
                isDefiledLoaded = net.minecraftforge.fml.ModList.get().isLoaded("defiledlands");
                isJMixinLoaded = net.minecraftforge.fml.ModList.get().isLoaded("jmixin");
                if (isIafLoaded)
                        LOGGER.info("IaF CE detected — BS weapons will use IaF materials.");
                if (isDefiledLoaded)
                        LOGGER.info("Defiled Lands detected — BS weapons will use Defiled materials.");
                if (isJMixinLoaded)
                        LOGGER.info("JMixin detected — chain lightning will be available.");

                IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

                ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);
                ModLoadingContext.get().registerConfig(Type.CLIENT, ModConfig.CLIENT_SPEC);

                ModItems.ITEMS.register(modEventBus);
                ModBlocks.BLOCKS.register(modEventBus);
                ModEnchantments.ENCHANTMENTS.register(modEventBus);
                ModEntities.ENTITY_TYPES.register(modEventBus);
                ModMobEffects.MOB_EFFECTS.register(modEventBus);
                ModPotions.POTIONS.register(modEventBus);
                ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
                CreativeTabInit.CREATIVE_TABS.register(modEventBus);
                ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);

                modEventBus.addListener(this::commonSetup);
                modEventBus.addListener(this::clientSetup);

                MinecraftForge.EVENT_BUS.register(this);

                LOGGER.info("BetterSurvival initializing...");
        }

        private void commonSetup(final FMLCommonSetupEvent event) {
                ModNetwork.register();

                event.enqueueWork(() -> {

                        addBrewing(Potions.AWKWARD, Items.INK_SAC, ModPotions.BLINDNESS.get());
                        addBrewing(ModPotions.BLINDNESS.get(), Items.REDSTONE, ModPotions.LONG_BLINDNESS.get());

                        addBrewing(Potions.REGENERATION, Items.FERMENTED_SPIDER_EYE, ModPotions.DECAY.get());
                        addBrewing(Potions.LONG_REGENERATION, Items.FERMENTED_SPIDER_EYE, ModPotions.LONG_DECAY.get());
                        addBrewing(Potions.STRONG_REGENERATION, Items.FERMENTED_SPIDER_EYE,
                                        ModPotions.STRONG_DECAY.get());
                        addBrewing(ModPotions.DECAY.get(), Items.REDSTONE, ModPotions.LONG_DECAY.get());
                        addBrewing(ModPotions.DECAY.get(), Items.GLOWSTONE_DUST, ModPotions.STRONG_DECAY.get());

                        addBrewing(Potions.AWKWARD, Items.CHORUS_FRUIT, ModPotions.WARP.get());
                        addBrewing(ModPotions.WARP.get(), Items.GLOWSTONE_DUST, ModPotions.STRONG_WARP.get());

                        addBrewing(ModPotions.WARP.get(), Items.FERMENTED_SPIDER_EYE, ModPotions.ANTIWARP.get());
                        addBrewing(ModPotions.ANTIWARP.get(), Items.REDSTONE, ModPotions.LONG_ANTIWARP.get());

                        addBrewing(ModPotions.MILK.get(), Items.FERMENTED_SPIDER_EYE, ModPotions.DISPEL.get());
                        addBrewing(ModPotions.MILK.get(), Items.GOLDEN_APPLE, ModPotions.CURE.get());
                });

                event.enqueueWork(() -> {

                        net.minecraft.core.cauldron.CauldronInteraction.EMPTY.put(Items.POTION,
                                        (state, level, pos, player, hand, stack) -> {
                                                net.minecraft.world.item.alchemy.Potion potion = PotionUtils
                                                                .getPotion(stack);
                                                if (potion == Potions.WATER || potion == Potions.EMPTY)
                                                        return InteractionResult.PASS;
                                                if (level.isClientSide)
                                                        return InteractionResult.SUCCESS;
                                                if (!player.getAbilities().instabuild) {
                                                        stack.shrink(1);
                                                        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                                                        if (stack.isEmpty())
                                                                player.setItemInHand(hand, bottle);
                                                        else if (!player.getInventory().add(bottle))
                                                                player.drop(bottle, false);
                                                }
                                                level.playSound(null, pos,
                                                                net.minecraft.sounds.SoundEvents.BOTTLE_EMPTY,
                                                                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                                                BlockState newState = ModBlocks.POTION_CAULDRON.get()
                                                                .defaultBlockState()
                                                                .setValue(com.mx.bettersurvival.blocks.PotionCauldronBlock.LEVEL,
                                                                                1);
                                                level.setBlockAndUpdate(pos, newState);
                                                net.minecraft.world.level.block.entity.BlockEntity be = level
                                                                .getBlockEntity(pos);
                                                if (be instanceof com.mx.bettersurvival.blocks.PotionCauldronBlockEntity pbe) {
                                                        pbe.setStoredPotion(potion);
                                                }
                                                return InteractionResult.SUCCESS;
                                        });

                        net.minecraft.core.cauldron.CauldronInteraction.EMPTY.put(Items.MILK_BUCKET,
                                        (state, level, pos, player, hand, stack) -> {
                                                if (level.isClientSide)
                                                        return InteractionResult.SUCCESS;
                                                if (!player.getAbilities().instabuild) {
                                                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                                                }
                                                level.playSound(null, pos,
                                                                net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                                                                net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                                                BlockState newState = ModBlocks.POTION_CAULDRON.get()
                                                                .defaultBlockState()
                                                                .setValue(com.mx.bettersurvival.blocks.PotionCauldronBlock.LEVEL,
                                                                                3);
                                                level.setBlockAndUpdate(pos, newState);
                                                net.minecraft.world.level.block.entity.BlockEntity be = level
                                                                .getBlockEntity(pos);
                                                if (be instanceof com.mx.bettersurvival.blocks.PotionCauldronBlockEntity pbe) {
                                                        pbe.setMilk(true);
                                                }
                                                return InteractionResult.SUCCESS;
                                        });
                });

                LOGGER.info("BetterSurvival common setup");
        }

        private static void addBrewing(Potion input, net.minecraft.world.level.ItemLike reagent, Potion output) {
                BrewingRecipeRegistry.addRecipe(
                                Ingredient.of(PotionUtils.setPotion(new ItemStack(Items.POTION), input)),
                                Ingredient.of(reagent),
                                PotionUtils.setPotion(new ItemStack(Items.POTION), output));
        }

        private void clientSetup(final FMLClientSetupEvent event) {
                LOGGER.info("BetterSurvival client setup");
        }
}
