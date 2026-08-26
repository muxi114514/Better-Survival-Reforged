package com.mx.bettersurvival.network;

import com.mx.bettersurvival.BetterSurvival;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(BetterSurvival.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SpinningPacket.class,
                SpinningPacket::encode,
                SpinningPacket::decode,
                SpinningPacket::handle);
        CHANNEL.registerMessage(id++, GuardStaminaSyncPacket.class,
                GuardStaminaSyncPacket::encode,
                GuardStaminaSyncPacket::decode,
                GuardStaminaSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
