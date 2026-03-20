package com.chen1335.ultimateEnchantment.mixins.touhouLittleMaid;

import com.chen1335.ultimateEnchantment.mixinsAPI.touhouLittleMaid.hooks.EntityMaidHook;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityMaid.class)
public abstract class EntityMaidMixin implements RangedAttackMob {
    //给开发游戏环境注入,正常游戏环境会注入失败但是不影响:(((((
    @Inject(method = "performRangedAttack", at = @At("RETURN"), remap = false)
    private void performRangedAttack(LivingEntity target, float distanceFactor, CallbackInfo ci) {
        EntityMaidHook.performRangedAttack((EntityMaid) (Object) this, target, distanceFactor, ci);
    }

    //给正常游戏环境注入,开发环境会注入失败但是不影响:(((((
    @Inject(method = "m_6504_", at = @At("RETURN"), remap = false)
    private void performRangedAttack1(LivingEntity target, float distanceFactor, CallbackInfo ci) {
        EntityMaidHook.performRangedAttack((EntityMaid) (Object) this, target, distanceFactor, ci);
    }
}
