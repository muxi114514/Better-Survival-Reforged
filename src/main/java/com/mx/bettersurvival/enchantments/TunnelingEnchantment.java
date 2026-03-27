package com.mx.bettersurvival.enchantments;

import com.mx.bettersurvival.config.ModConfig;
import com.mx.bettersurvival.init.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TunnelingEnchantment extends Enchantment {

    public TunnelingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    public static void mineManyBlocks(Player miner, BlockState state, BlockPos pos) {
        Level level = miner.level();
        ItemStack stack = miner.getMainHandItem();
        int l = EnchantmentHelper.getEnchantmentLevel(ModEnchantments.TUNNELING.get(), miner);

        if (l > 0 && canMineEffectively(miner, state, pos)) {
            Direction facing = Direction.getNearest(
                    (float) (pos.getX() + 0.5 - miner.getX()),
                    (float) (pos.getY() + 0.5 - miner.getEyeY()),
                    (float) (pos.getZ() + 0.5 - miner.getZ())).getOpposite();

            Direction.Axis excludedAxis = facing.getAxis();

            for (int x = -l; x <= l; x++) {
                if (excludedAxis == Direction.Axis.X && x != 0)
                    continue;
                for (int y = -l; y <= l; y++) {
                    if (excludedAxis == Direction.Axis.Y && y != 0)
                        continue;
                    for (int z = -l; z <= l; z++) {
                        if (excludedAxis == Direction.Axis.Z && z != 0)
                            continue;
                        if (x == 0 && y == 0 && z == 0)
                            continue;
                        if (Math.sqrt(x * x + y * y + z * z) > (l + 1.0F) / 2.0F)
                            continue;

                        BlockPos pos1 = pos.offset(x, y, z);
                        if (canMineEffectively(miner, level.getBlockState(pos1), pos1)) {
                            ((ServerPlayer) miner).gameMode.destroyBlock(pos1);
                        }
                    }
                }
            }
        }
    }

    static boolean canMineEffectively(Player player, BlockState state, BlockPos pos) {
        if (state.isAir())
            return false;
        if (ModConfig.COMMON.preventTunnelingBlockEntities.get() && player.level().getBlockEntity(pos) != null)
            return false;

        ItemStack stack = player.getMainHandItem();
        return stack.isCorrectToolForDrops(state);
    }

    @Override
    public int getMaxLevel() {
        return ModConfig.COMMON.tunnelingLevel.get();
    }

    @Override
    public int getMinCost(int level) {
        return 25 + (level - 1) * 15;
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return ModConfig.COMMON.tunnelingTreasure.get();
    }

    @Override
    public boolean isAllowedOnBooks() {
        return ModConfig.COMMON.tunnelingLevel.get() != 0;
    }
}
