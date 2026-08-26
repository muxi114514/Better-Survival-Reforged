package com.mx.bettersurvival.network;

import com.mx.bettersurvival.client.GuardStaminaClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端 → 本人客户端：同步格挡精力与破防冷却，供 HUD 显示与攻击闸门读取。
 */
public class GuardStaminaSyncPacket {

    private final float stamina;
    private final int cooldown;

    public GuardStaminaSyncPacket(float stamina, int cooldown) {
        this.stamina = stamina;
        this.cooldown = cooldown;
    }

    public static void encode(GuardStaminaSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.stamina);
        buf.writeVarInt(msg.cooldown);
    }

    public static GuardStaminaSyncPacket decode(FriendlyByteBuf buf) {
        return new GuardStaminaSyncPacket(buf.readFloat(), buf.readVarInt());
    }

    public static void handle(GuardStaminaSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        // 客户端类经 DistExecutor 隔离，避免专用服务端加载客户端类。
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> GuardStaminaClient.apply(msg.stamina, msg.cooldown)));
        ctx.get().setPacketHandled(true);
    }
}
