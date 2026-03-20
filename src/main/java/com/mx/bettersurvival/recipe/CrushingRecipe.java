package com.mx.bettersurvival.recipe;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CrushingRecipe {

    private static final CrushingRecipe CRUSHING_BASE = new CrushingRecipe();
    private final Map<BlockState, BlockState> crushingList = Maps.newHashMap();

    public static CrushingRecipe instance() {
        return CRUSHING_BASE;
    }

    public static void crush(Player player, BlockPos pos, boolean particles) {
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel && particles) {
            serverLevel.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        BlockState result = CrushingRecipe.instance().getCrushingResult(level.getBlockState(pos));
        if (result != null) {
            // Handle infested blocks – spawn silverfish
            BlockState current = level.getBlockState(pos);
            if (isInfestedBlock(current) && !level.isClientSide) {
                Silverfish silverfish = new Silverfish(net.minecraft.world.entity.EntityType.SILVERFISH, level);
                silverfish.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
                level.addFreshEntity(silverfish);
                double attackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                silverfish.hurt(player.damageSources().playerAttack(player), (float) attackDamage);
            }
            level.setBlockAndUpdate(pos, result);
        }
    }

    private static boolean isInfestedBlock(BlockState state) {
        return state.is(Blocks.INFESTED_STONE) ||
                state.is(Blocks.INFESTED_COBBLESTONE) ||
                state.is(Blocks.INFESTED_STONE_BRICKS) ||
                state.is(Blocks.INFESTED_MOSSY_STONE_BRICKS) ||
                state.is(Blocks.INFESTED_CRACKED_STONE_BRICKS) ||
                state.is(Blocks.INFESTED_CHISELED_STONE_BRICKS) ||
                state.is(Blocks.INFESTED_DEEPSLATE);
    }

    public void addCrushingRecipe(BlockState target, BlockState result) {
        if (target != null && result != null) {
            if (getCrushingResult(target) == null) {
                crushingList.put(target, result);
            }
        }
    }

    public BlockState getCrushingResult(BlockState target) {
        for (Map.Entry<BlockState, BlockState> entry : this.crushingList.entrySet()) {
            if (target == entry.getKey()) {
                return entry.getValue();
            }
        }
        return null;
    }

    private CrushingRecipe() {
        // Stone variants
        addCrushingRecipe(Blocks.STONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState());
        addCrushingRecipe(Blocks.COBBLESTONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
        addCrushingRecipe(Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());

        // Sandstone
        addCrushingRecipe(Blocks.SANDSTONE.defaultBlockState(), Blocks.SAND.defaultBlockState());
        addCrushingRecipe(Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState());
        addCrushingRecipe(Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.SANDSTONE.defaultBlockState());
        addCrushingRecipe(Blocks.RED_SANDSTONE.defaultBlockState(), Blocks.RED_SAND.defaultBlockState());
        addCrushingRecipe(Blocks.CHISELED_RED_SANDSTONE.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState());
        addCrushingRecipe(Blocks.CUT_RED_SANDSTONE.defaultBlockState(), Blocks.RED_SANDSTONE.defaultBlockState());

        // Stone bricks
        addCrushingRecipe(Blocks.STONE_BRICKS.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState());

        // Polished stone variants → base form
        addCrushingRecipe(Blocks.POLISHED_GRANITE.defaultBlockState(), Blocks.GRANITE.defaultBlockState());
        addCrushingRecipe(Blocks.POLISHED_DIORITE.defaultBlockState(), Blocks.DIORITE.defaultBlockState());
        addCrushingRecipe(Blocks.POLISHED_ANDESITE.defaultBlockState(), Blocks.ANDESITE.defaultBlockState());

        // Prismarine
        addCrushingRecipe(Blocks.PRISMARINE_BRICKS.defaultBlockState(), Blocks.PRISMARINE.defaultBlockState());

        // Grass → Dirt
        addCrushingRecipe(Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState());

        // Infested blocks → their non-infested crushed result
        addCrushingRecipe(Blocks.INFESTED_STONE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState());
        addCrushingRecipe(Blocks.INFESTED_COBBLESTONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
        addCrushingRecipe(Blocks.INFESTED_STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.INFESTED_MOSSY_STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.INFESTED_CRACKED_STONE_BRICKS.defaultBlockState(),
                Blocks.COBBLESTONE.defaultBlockState());
        addCrushingRecipe(Blocks.INFESTED_CHISELED_STONE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_STONE_BRICKS.defaultBlockState());

        // Deepslate (1.20.1 addition)
        addCrushingRecipe(Blocks.DEEPSLATE.defaultBlockState(), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        addCrushingRecipe(Blocks.COBBLED_DEEPSLATE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
        addCrushingRecipe(Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        addCrushingRecipe(Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        addCrushingRecipe(Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(),
                Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        addCrushingRecipe(Blocks.DEEPSLATE_TILES.defaultBlockState(),
                Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState());
        addCrushingRecipe(Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(),
                Blocks.COBBLED_DEEPSLATE.defaultBlockState());

        // Wool → Carpet (all 16 colors)
        addCrushingRecipe(Blocks.WHITE_WOOL.defaultBlockState(), Blocks.WHITE_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.ORANGE_WOOL.defaultBlockState(), Blocks.ORANGE_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.MAGENTA_WOOL.defaultBlockState(), Blocks.MAGENTA_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.LIGHT_BLUE_WOOL.defaultBlockState(), Blocks.LIGHT_BLUE_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.YELLOW_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.LIME_WOOL.defaultBlockState(), Blocks.LIME_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.PINK_WOOL.defaultBlockState(), Blocks.PINK_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.GRAY_WOOL.defaultBlockState(), Blocks.GRAY_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.LIGHT_GRAY_WOOL.defaultBlockState(), Blocks.LIGHT_GRAY_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.CYAN_WOOL.defaultBlockState(), Blocks.CYAN_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.PURPLE_WOOL.defaultBlockState(), Blocks.PURPLE_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.BLUE_WOOL.defaultBlockState(), Blocks.BLUE_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.BROWN_WOOL.defaultBlockState(), Blocks.BROWN_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.GREEN_WOOL.defaultBlockState(), Blocks.GREEN_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.RED_WOOL.defaultBlockState(), Blocks.RED_CARPET.defaultBlockState());
        addCrushingRecipe(Blocks.BLACK_WOOL.defaultBlockState(), Blocks.BLACK_CARPET.defaultBlockState());
    }
}
