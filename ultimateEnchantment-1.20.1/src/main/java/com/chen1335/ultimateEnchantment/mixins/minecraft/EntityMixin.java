package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract void setNoGravity(boolean pNoGravity);

    @Shadow
    public abstract void setDeltaMovement(double pX, double pY, double pZ);

    @Inject(method = "onBelowWorld", at = @At("HEAD"), cancellable = true)
    private void onBelowWorld(CallbackInfo ci) {
        if ((Entity) (Object) this instanceof ItemEntity itemEntity && itemEntity.getItem().getEnchantmentLevel(Enchantments.ETERNAL.get()) > 0) {
            this.setNoGravity(true);
            this.setDeltaMovement(0, 1.5, 0);
            ci.cancel();
        }
    }
}
