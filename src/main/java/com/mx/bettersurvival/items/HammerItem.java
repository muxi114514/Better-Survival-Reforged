package com.mx.bettersurvival.items;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.init.ModMobEffects;
import com.mx.bettersurvival.recipe.CrushingRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class HammerItem extends CustomWeaponItem {

    public static final int STUN_DURATION = 50;

    public HammerItem(Tier tier, float damageModifier, float speedModifier, Properties properties) {
        super(tier, damageModifier, speedModifier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null)
            return InteractionResult.PASS;

        if (context.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();

        float d = Math.min(super.getAttackDamage(), 9) + 1;

        double xd = 1, yd = 1, zd = 1;
        if (facing == Direction.UP || facing == Direction.DOWN) {
            xd += 9;
            zd += 9;
        } else if (facing == Direction.EAST || facing == Direction.WEST) {
            yd += 9;
            zd += 9;
        } else if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            xd += 9;
            yd += 9;
        }

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(xd, yd, zd))) {
            if (entity != player && entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 2 * d
                    && entity.onGround()) {
                entity.knockback(0.4F,
                        Mth.sin(player.getYRot() * 0.017453292F),
                        -Mth.cos(player.getYRot() * 0.017453292F));
                float distFactor = (float) (1 - entity.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) / 20.0F);
                entity.hurt(player.damageSources().playerAttack(player),
                        (getAttackDamage() / 2.0F + 1.5F) * distFactor);

                if (ModMobEffects.STUN.isPresent()) {
                    entity.addEffect(new MobEffectInstance(ModMobEffects.STUN.get(), STUN_DURATION));
                }
            }
        }

        if (player.getAbilities().mayBuild) {
            for (int x = -5; x <= 5; x++) {
                if ((facing == Direction.WEST || facing == Direction.EAST) && x != 0)
                    continue;
                for (int y = -5; y <= 5; y++) {
                    if ((facing == Direction.UP || facing == Direction.DOWN) && y != 0)
                        continue;
                    for (int z = -5; z <= 5; z++) {
                        if ((facing == Direction.NORTH || facing == Direction.SOUTH) && z != 0)
                            continue;
                        if (x * x + y * y + z * z <= 2.0f * d) {
                            boolean particles = x * x + y * y + z * z >= 2.0f * (d - 2.0f);
                            BlockPos position = pos.offset(x, y, z);

                            destroyGlassAt(level, position.relative(facing));
                            destroyGlassAt(level, position.relative(facing.getOpposite()));
                            destroyGlassAt(level, position);

                            BlockState faceState = level.getBlockState(position.relative(facing));
                            if (faceState.isFaceSturdy(level, position, Direction.UP)) {
                                BlockPos pos2 = position.relative(facing);
                                BlockState beyondState = level.getBlockState(pos2.relative(facing));
                                if (!beyondState.isFaceSturdy(level, pos2, Direction.UP)) {
                                    CrushingRecipe.crush(player, pos2, particles);
                                }
                            } else if (!level.getBlockState(position).isFaceSturdy(level, position, Direction.UP)) {
                                CrushingRecipe.crush(player, position.relative(facing.getOpposite()), particles);
                            } else {
                                CrushingRecipe.crush(player, position, particles);
                            }
                        }
                    }
                }
            }
        }

        if (!player.isCreative()) {
            context.getItemInHand().hurtAndBreak(10, player,
                    (p) -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }

        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS,
                1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);

        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(this, 200);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void destroyGlassAt(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() instanceof AbstractGlassBlock) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        target.knockback(0.5F,
                Mth.sin(attacker.getYRot() * 0.017453292F),
                -Mth.cos(attacker.getYRot() * 0.017453292F));
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(BetterSurvival.MOD_ID + ".hammer.desc")
                .withStyle(ChatFormatting.AQUA));
    }
}
