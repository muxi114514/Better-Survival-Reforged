package com.chen1335.ultimateEnchantment.mixins.touhouLittleMaid;

import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.mixinsAPI.minecraft.api.IAbstractArrowExtension;
import com.chen1335.ultimateEnchantment.mixinsAPI.touhouLittleMaid.api.ITaskBowAttackExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskBowAttack;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = TaskBowAttack.class, remap = false)
public abstract class TaskBowAttackMixin implements ITaskBowAttackExtension {

    @Shadow
    @Nullable
    protected abstract AbstractArrow getArrow(EntityMaid maid, float chargeTime);

    @Inject(method = "getArrow", at = @At("RETURN"), cancellable = true)
    private void getArrow(EntityMaid maid, float chargeTime, CallbackInfoReturnable<AbstractArrow> cir) {
        if (maid.getMainHandItem().getEnchantmentLevel(Enchantments.TERMINATOR.get()) > 0) {
            IAbstractArrowExtension arrowExtension = (IAbstractArrowExtension) cir.getReturnValue();
            if (arrowExtension != null) {
                arrowExtension.ue$setByPassInvulnerableTime(true);
                cir.setReturnValue((AbstractArrow) arrowExtension);
            }
        }
    }

    @Unique
    public AbstractArrow ue$getArrow(EntityMaid maid, float chargeTime) {
        return this.getArrow(maid, chargeTime);
    }
}
