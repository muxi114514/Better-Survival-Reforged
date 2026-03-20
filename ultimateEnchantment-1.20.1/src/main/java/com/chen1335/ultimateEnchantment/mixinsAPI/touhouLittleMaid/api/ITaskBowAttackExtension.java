package com.chen1335.ultimateEnchantment.mixinsAPI.touhouLittleMaid.api;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.entity.projectile.AbstractArrow;

public interface ITaskBowAttackExtension {
    AbstractArrow ue$getArrow(EntityMaid maid, float chargeTime);
}
