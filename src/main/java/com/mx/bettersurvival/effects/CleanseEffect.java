package com.mx.bettersurvival.effects;

import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CleanseEffect extends InstantenousMobEffect {

    public CleanseEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        cleanse(entity);
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource,
            LivingEntity target, int amplifier, double health) {
        cleanse(target);
    }

    private void cleanse(LivingEntity entity) {
        boolean thisIsBad = this.getCategory() == MobEffectCategory.HARMFUL;
        ItemStack milk = new ItemStack(Items.MILK_BUCKET);

        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance instance : entity.getActiveEffects()) {
            if (!instance.isCurativeItem(milk))
                continue;

            boolean effectIsBad = instance.getEffect().getCategory() == MobEffectCategory.HARMFUL;

            if (thisIsBad && !effectIsBad) {
                toRemove.add(instance.getEffect());
            } else if (!thisIsBad && effectIsBad) {
                toRemove.add(instance.getEffect());
            }
        }

        for (MobEffect effect : toRemove) {
            entity.removeEffect(effect);
        }
    }
}
