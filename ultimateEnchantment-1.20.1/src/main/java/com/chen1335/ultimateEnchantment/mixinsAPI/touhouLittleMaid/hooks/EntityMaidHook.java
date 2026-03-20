package com.chen1335.ultimateEnchantment.mixinsAPI.touhouLittleMaid.hooks;

import com.chen1335.ultimateEnchantment.enchantment.Enchantments;
import com.chen1335.ultimateEnchantment.enchantment.enchantments.Terminator;
import com.chen1335.ultimateEnchantment.mixinsAPI.touhouLittleMaid.api.ITaskBowAttackExtension;
import com.chen1335.ultimateEnchantment.utils.SimpleSchedule;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskBowAttack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class EntityMaidHook {

    public static void performRangedAttack(EntityMaid entityMaid, LivingEntity target, float distanceFactor, CallbackInfo ci) {
        ItemStack mainHandItem = entityMaid.getMainHandItem();
        if (mainHandItem.getItem() instanceof BowItem || mainHandItem.getTags().anyMatch(itemTagKey -> itemTagKey == Tags.Items.TOOLS_BOWS)) {
            if (mainHandItem.getEnchantmentLevel(Enchantments.TERMINATOR.get()) > 0) {
                IMaidTask maidTask = entityMaid.getTask();
                if (maidTask instanceof TaskBowAttack taskBowAttack) {
                    multipleShoot(taskBowAttack, entityMaid, target, distanceFactor);
                    SimpleSchedule.addSchedule(entityMaid.level(), new SimpleSchedule.Wait(() -> {
                        taskBowAttack.performRangedAttack(entityMaid, target, distanceFactor);
                        multipleShoot(taskBowAttack, entityMaid, target, distanceFactor);
                    }, 5));
                }
            }
        }
    }

    public static void multipleShoot(TaskBowAttack taskBowAttack, EntityMaid shooter, LivingEntity target, float distanceFactor) {
        for (int side : Terminator.SIDES) {
            AbstractArrow entityArrow = ((ITaskBowAttackExtension) taskBowAttack).ue$getArrow(shooter, distanceFactor);
            if (entityArrow != null) {
                ItemStack mainHandItem = shooter.getMainHandItem();
                if (mainHandItem.getItem() instanceof BowItem) {
                    double x = target.getX() - shooter.getX();
                    double y = target.getBoundingBox().minY + (double) (target.getBbHeight() / 3.0F) - entityArrow.position().y;
                    double z = target.getZ() - shooter.getZ();
                    double pitch = Math.sqrt(x * x + z * z) * 0.15;
                    entityArrow.shoot(x + side, y + pitch, z, 1.6F, 1.0F);
                    shooter.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (shooter.getRandom().nextFloat() * 0.4F + 0.8F));
                    shooter.level().addFreshEntity(entityArrow);
                }
            }
        }
    }

}
