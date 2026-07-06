package com.mx.bettersurvival.init;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

public class ModTiers {

        public static final ForgeTier EMERALD = new ForgeTier(
                        3,
                        1200,
                        9.0F,
                        3.0F,
                        20,
                        BlockTags.NEEDS_DIAMOND_TOOL,
                        () -> Ingredient.of(Items.EMERALD));

        public static final ForgeTier CRYING_OBSIDIAN = new ForgeTier(
                        3,
                        1800,
                        7.0F,
                        3.5F,
                        12,
                        BlockTags.NEEDS_DIAMOND_TOOL,
                        () -> Ingredient.of(Items.CRYING_OBSIDIAN));

        public static final ForgeTier NETHERITE = new ForgeTier(
                        3,
                        2000,
                        10.0F,
                        5.5F,
                        22,
                        BlockTags.NEEDS_DIAMOND_TOOL,
                        () -> Ingredient.of(Items.NETHERITE_INGOT));

        // SRP 风味材料（感知/觉知）：常驻注册、无配方（暂无高版本寄生虫模组可依赖），龙钢档
        public static final ForgeTier LIVING = new ForgeTier(
                        4,
                        4000,
                        4.0F,
                        10.0F,
                        10,
                        BlockTags.NEEDS_DIAMOND_TOOL,
                        () -> Ingredient.EMPTY);

        public static final ForgeTier SENTIENT = new ForgeTier(
                        4,
                        4000,
                        4.0F,
                        10.0F,
                        10,
                        BlockTags.NEEDS_DIAMOND_TOOL,
                        () -> Ingredient.EMPTY);
}
