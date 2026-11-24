package com.mx.bettersurvival.client;

import com.mx.bettersurvival.capability.ModCapabilities;
import com.mx.bettersurvival.items.NunchakuItem;
import com.mx.bettersurvival.network.ModNetwork;
import com.mx.bettersurvival.network.SpinningPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.entity.PartEntity;

/**
 * Client-only nunchaku logic: the hold-to-spin attack loop and applying spin state
 * received from the server for other players. Kept in its own class so the server
 * never loads client classes through the network packets.
 */
public final class NunchakuClient {

    private NunchakuClient() {
    }

    /** Called every client tick: holding attack with a nunchaku spins it and auto-attacks. */
    public static void tickSpin(Minecraft mc, LocalPlayer player) {
        boolean holdingNunchaku = player.getMainHandItem().getItem() instanceof NunchakuItem;
        boolean attackKeyDown = mc.options.keyAttack.isDown();

        player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
            if (holdingNunchaku && attackKeyDown
                    && !player.isSpectator()
                    && player.getUseItem().isEmpty()) {

                if (!combo.isSpinning()) {
                    combo.setSpinning(true);
                    ModNetwork.CHANNEL.sendToServer(new SpinningPacket(true));
                }

                if (player.getAttackStrengthScale(0.5F) >= 1.0F) {
                    Entity target = pickTarget(mc, player);
                    if (target != null) {
                        // gameMode.attack resets the attack strength ticker itself
                        mc.gameMode.attack(player, target);
                    }
                }
            } else if (combo.isSpinning()) {
                combo.setSpinning(false);
                ModNetwork.CHANNEL.sendToServer(new SpinningPacket(false));
            }
        });
    }

    /**
     * The entity under the crosshair, if it can be attacked. Multipart entities (e.g. dragons)
     * are resolved to their parent so the player can't hit themselves or their own mount.
     */
    private static Entity pickTarget(Minecraft mc, LocalPlayer player) {
        HitResult hit = mc.hitResult;
        if (!(hit instanceof EntityHitResult entityHit))
            return null;
        Entity target = entityHit.getEntity();
        if (!target.isAlive() || !target.isAttackable())
            return null;
        Entity owner = target instanceof PartEntity<?> part ? part.getParent() : target;
        if (owner == player || owner.getRootVehicle() == player.getRootVehicle())
            return null;
        return target;
    }

    /** Applies a spin state sent by the server (SpinningSyncPacket) to another player. */
    public static void applySpinning(int entityId, boolean spinning) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null)
            return;
        Entity entity = mc.level.getEntity(entityId);
        if (entity instanceof Player) {
            entity.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> combo.setSpinning(spinning));
        }
    }
}
