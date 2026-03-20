package com.mx.bettersurvival.network;

import com.mx.bettersurvival.capability.ModCapabilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server packet: sync nunchaku spinning state.
 * Mirrors original MessageNunchakuSpinClient from 1.12.
 */
public class SpinningPacket {

    private final boolean spinning;

    public SpinningPacket(boolean spinning) {
        this.spinning = spinning;
    }

    public static void encode(SpinningPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.spinning);
    }

    public static SpinningPacket decode(FriendlyByteBuf buf) {
        return new SpinningPacket(buf.readBoolean());
    }

    public static void handle(SpinningPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null)
                return;

            player.getCapability(ModCapabilities.NUNCHAKU_COMBO).ifPresent(combo -> {
                combo.setSpinning(msg.spinning);
                if (!msg.spinning) {
                    combo.setComboTime(0);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
