package com.mx.bettersurvival.network;

import com.mx.bettersurvival.client.NunchakuClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: another player's nunchaku spin state, so their spin animation shows. */
public class SpinningSyncPacket {

    private final int entityId;
    private final boolean spinning;

    public SpinningSyncPacket(int entityId, boolean spinning) {
        this.entityId = entityId;
        this.spinning = spinning;
    }

    public static void encode(SpinningSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.spinning);
    }

    public static SpinningSyncPacket decode(FriendlyByteBuf buf) {
        return new SpinningSyncPacket(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SpinningSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> NunchakuClient.applySpinning(msg.entityId, msg.spinning)));
        ctx.get().setPacketHandled(true);
    }
}
