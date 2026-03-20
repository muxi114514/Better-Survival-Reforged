package com.chen1335.ultimateEnchantment.mixins.minecraft;

import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAbstractArrowExtension;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile implements IAbstractArrowExtension {
    @Shadow
    private int life;
    @Shadow
    protected int inGroundTime;
    @Unique
    private boolean ue$byPassInvulnerableTime = false;
    @Unique
    private boolean ue$shootByTerminator = false;

    protected AbstractArrowMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void init(EntityType<?> pEntityType, LivingEntity pShooter, Level pLevel, CallbackInfo ci) {
        ItemStack useItem = pShooter.getUseItem();

        if (!useItem.isEmpty()) {
            if (useItem.getEnchantmentLevel(Enchantments.TERMINATOR.get()) > 0) {
                ue$byPassInvulnerableTime = true;
                ue$shootByTerminator = true;
            }
        }
    }


    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (this.inGroundTime >= 20 && ue$shootByTerminator) {
            this.discard();
        }
    }

    @Unique
    public boolean ue$isByPassInvulnerableTime() {
        return ue$byPassInvulnerableTime;
    }

    @Unique
    public void ue$setByPassInvulnerableTime(boolean ue$byPassInvulnerableTime) {
        this.ue$byPassInvulnerableTime = ue$byPassInvulnerableTime;
    }
}
