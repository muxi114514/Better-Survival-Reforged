package com.mx.bettersurvival.blocks;

import com.mx.bettersurvival.init.ModBlocks;
import com.mx.bettersurvival.init.ModPotions;
import com.mx.bettersurvival.items.CustomWeaponItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Map;

public class PotionCauldronBlock extends AbstractCauldronBlock implements net.minecraft.world.level.block.EntityBlock {

    public static final IntegerProperty LEVEL = LayeredCauldronBlock.LEVEL;
    public static final int MAX_LEVEL = 3;

    public static final Map<Item, CauldronInteraction> INTERACTIONS = CauldronInteraction.newInteractionMap();

    public PotionCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, INTERACTIONS);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 1));
        registerInteractions();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) >= MAX_LEVEL;
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (6.0 + (double) state.getValue(LEVEL) * 3.0) / 16.0;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotionCauldronBlockEntity(pos, state);
    }

    public static void decreaseLevel(BlockState state, Level level, BlockPos pos) {
        int newLevel = state.getValue(LEVEL) - 1;
        BlockState newState = newLevel == 0
                ? Blocks.CAULDRON.defaultBlockState()
                : state.setValue(LEVEL, newLevel);
        level.setBlockAndUpdate(pos, newState);
    }

    private void registerInteractions() {

        INTERACTIONS.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity pbe))
                return InteractionResult.PASS;

            ItemStack result;
            if (pbe.isMilk()) {
                result = PotionUtils.setPotion(new ItemStack(Items.POTION), ModPotions.MILK.get());
            } else {
                result = pbe.createPotionStack();
            }
            if (result.isEmpty())
                return InteractionResult.PASS;

            stack.shrink(1);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, result);
            } else if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            decreaseLevel(state, level, pos);
            return InteractionResult.SUCCESS;
        });

        INTERACTIONS.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity pbe) || !pbe.isMilk())
                return InteractionResult.PASS;
            if (state.getValue(LEVEL) < MAX_LEVEL)
                return InteractionResult.PASS;

            stack.shrink(1);
            ItemStack bucket = new ItemStack(Items.MILK_BUCKET);
            if (stack.isEmpty()) {
                player.setItemInHand(hand, bucket);
            } else if (!player.getInventory().add(bucket)) {
                player.drop(bucket, false);
            }
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            return InteractionResult.SUCCESS;
        });

        INTERACTIONS.put(Items.ARROW, (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity pbe) || pbe.isMilk())
                return InteractionResult.PASS;

            ItemStack tipped = pbe.createTippedArrow();
            if (tipped.isEmpty())
                return InteractionResult.PASS;

            int count = Math.min(stack.getCount(), 8);
            tipped.setCount(count);
            stack.shrink(count);

            if (!player.getInventory().add(tipped)) {
                player.drop(tipped, false);
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                decreaseLevel(state, level, pos);
            }
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof CustomWeaponItem) {
            if (level.isClientSide)
                return InteractionResult.SUCCESS;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof PotionCauldronBlockEntity pbe) || pbe.isMilk()) {
                return super.use(state, level, pos, player, hand, hit);
            }

            Potion potion = pbe.getStoredPotion();
            if (potion == Potions.EMPTY || potion == Potions.WATER || potion == Potions.MUNDANE
                    || potion == Potions.THICK || potion == Potions.AWKWARD) {

                stack.removeTagKey("Potion");
                stack.removeTagKey("CustomPotionEffects");
                stack.removeTagKey("remainingPotionHits");
            } else {

                net.minecraft.resources.ResourceLocation potionId = net.minecraftforge.registries.ForgeRegistries.POTIONS
                        .getKey(potion);
                if (potionId != null && com.mx.bettersurvival.config.ModConfig.COMMON.potionBlacklist.get()
                        .contains(potionId.toString())) {
                    return super.use(state, level, pos, player, hand, hit);
                }

                int baseHits = com.mx.bettersurvival.config.ModConfig.COMMON.potionHitsBase.get();
                int maxHits = com.mx.bettersurvival.config.ModConfig.COMMON.potionHitsMax.get();

                var tag = stack.getOrCreateTag();
                int existingHits = tag.getInt("remainingPotionHits");
                if (existingHits > 0 && PotionUtils.getPotion(stack) == potion) {

                    tag.putInt("remainingPotionHits", Math.min(existingHits + baseHits, maxHits));
                } else {

                    PotionUtils.setPotion(stack, potion);
                    tag.putInt("remainingPotionHits", baseHits);
                }
            }

            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                decreaseLevel(state, level, pos);
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
    }
}
