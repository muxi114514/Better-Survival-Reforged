package com.mx.bettersurvival.effects;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class WarpEffect extends InstantenousMobEffect {

    public WarpEffect() {
        super(MobEffectCategory.NEUTRAL, 0x962EDF);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        teleport(entity, amplifier, 1.0);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource,
            LivingEntity target, int amplifier, double health) {
        teleport(target, amplifier, health);
    }

    private void teleport(LivingEntity entity, int amplifier, double healthFactor) {
        double distance = 16.0 * (amplifier + 1) * healthFactor;
        Level level = entity.level();
        if (level.isClientSide)
            return;

        double origX = entity.getX();
        double origY = entity.getY();
        double origZ = entity.getZ();

        for (int i = 0; i < 16; i++) {
            double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * distance;
            double y = Mth.clamp(entity.getY() + (entity.getRandom().nextInt(16) - 8),
                    level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
            double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * distance;

            if (entity.isPassenger()) {
                entity.stopRiding();
            }

            if (entity.randomTeleport(x, y, z, true)) {
                level.playSound(null, origX, origY, origZ,
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
                entity.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }
}
