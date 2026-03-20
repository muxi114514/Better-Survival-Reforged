package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.effect.MobEffects;
import com.chen1335.ultimateEnchantment.enchantment.EnchantmentEffectsHook;
import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique
    private float ue$oldHealth;

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract boolean removeEffect(MobEffect pEffect);

    @Shadow
    public abstract boolean addEffect(MobEffectInstance pEffectInstance);

    @Shadow
    @Nullable
    public abstract MobEffectInstance getEffect(MobEffect pEffect);

    @Shadow
    protected abstract void onEffectUpdated(MobEffectInstance pEffectInstance, boolean pForced, @org.jetbrains.annotations.Nullable Entity pEntity);

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (ue$oldHealth != this.getHealth()) {
            EnchantmentEffectsHook.onLivingHealthChange((LivingEntity) (Object) this);
            ue$oldHealth = this.getHealth();
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    private void addEffect(MobEffectInstance pEffectInstance, Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        MobEffectInstance activeVanquisher = this.getEffect(MobEffects.ACTIVE_VANQUISHER.get());
        if (pEffectInstance.getEffect() == MobEffects.UN_ACTIVE_VANQUISHER.get()) {
            if (pEffectInstance.getAmplifier() + 1 >= 10 && activeVanquisher == null) {
                this.removeEffect(MobEffects.UN_ACTIVE_VANQUISHER.get());
                this.addEffect(new MobEffectInstance(MobEffects.ACTIVE_VANQUISHER.get(), Enchantments.VANQUISHER.get().buffDuration, 0, false, false, true));
            } else if (activeVanquisher != null) {
                this.removeEffect(MobEffects.UN_ACTIVE_VANQUISHER.get());
                MobEffectInstance newActiveVanquisher = new MobEffectInstance(MobEffects.ACTIVE_VANQUISHER.get(), Enchantments.VANQUISHER.get().buffDuration, 0, false, false, true);

                activeVanquisher.update(newActiveVanquisher);
                this.onEffectUpdated(newActiveVanquisher, true, null);
            }
        }
    }
}
