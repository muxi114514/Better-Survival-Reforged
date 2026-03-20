package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.blocks.PotionCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            BetterSurvival.MOD_ID);

    public static final RegistryObject<Block> POTION_CAULDRON = BLOCKS.register("potion_cauldron",
            () -> new PotionCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)));
}
