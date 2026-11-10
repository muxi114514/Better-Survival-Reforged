package com.mx.bettersurvival.events;

import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.items.NunchakuItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nunchaku hits deal no knockback, so a spinning combo keeps the target in reach.
 * Entities hit by a nunchaku melee attack are remembered for the rest of the tick
 * and their knockback event is cancelled.
 */
@Mod.EventBusSubscriber(modid = BetterSurvival.MOD_ID)
public final class NunchakuKnockbackHandler {

    private static final Set<Integer> HIT_ENTITY_IDS = ConcurrentHashMap.newKeySet();

    private NunchakuKnockbackHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide)
            return;
        DamageSource source = event.getSource();
        if (com.mx.bettersurvival.combat.MeleeHits.isMeleeHit(source)
                && source.getDirectEntity() instanceof Player attacker
                && attacker.getMainHandItem().getItem() instanceof NunchakuItem) {
            HIT_ENTITY_IDS.add(target.getId());
        }
    }

    @SubscribeEvent
    public static void onKnockBack(LivingKnockBackEvent event) {
        if (HIT_ENTITY_IDS.isEmpty() || event.getEntity().level().isClientSide)
            return;
        if (HIT_ENTITY_IDS.contains(event.getEntity().getId())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (!HIT_ENTITY_IDS.isEmpty())
            HIT_ENTITY_IDS.clear();
    }
}
