package com.mx.bettersurvival.init;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.blocks.PotionCauldronBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, BetterSurvival.MOD_ID);

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<PotionCauldronBlockEntity>> POTION_CAULDRON = BLOCK_ENTITIES
            .register("potion_cauldron",
                    () -> BlockEntityType.Builder.of(PotionCauldronBlockEntity::new, ModBlocks.POTION_CAULDRON.get())
                            .build(null));
}
