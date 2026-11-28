package com.mx.bettersurvival.gametest;

import com.mojang.authlib.GameProfile;
import com.mx.bettersurvival.BetterSurvival;
import com.mx.bettersurvival.network.ModNetwork;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dev-only helpers for Better Survival's bug-reproduction GameTests (never shipped).
 * Every test asserts the CORRECT behaviour: a failure means the scan's bug reproduced, and the same test passing after
 * the fix proves the fix. Each result is logged as one [BSREPRO] line.
 */
public final class TestKit {
    private static int count;

    private TestKit() {}

    /** A mock player with a real (in-memory) connection, so packets and chat work. Keeps its channel for {@link #chat}. */
    public record Mock(ServerPlayer player, EmbeddedChannel channel) {}

    /**
     * A logged-in mock player at the relative block (x, y, z). Vanilla's makeMockServerPlayerInLevel has no netty
     * channel and Forge NPEs on it; an EmbeddedChannel gives the Connection one (channelActive sets it).
     * opLevel 0 = a normal player; 2+ = an operator at that permission level.
     */
    public static Mock player(GameTestHelper helper, int x, int y, int z, GameType mode, int opLevel) {
        ServerLevel level = helper.getLevel();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "bstest-" + (++count));
        ServerPlayer player = new ServerPlayer(level.getServer(), level, profile);
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player);
        if (opLevel > 0) {
            // PlayerList.op() would use the server's default level; add the entry with the level we want
            level.getServer().getPlayerList().getOps().add(new ServerOpListEntry(profile, opLevel, false));
            level.getServer().getPlayerList().sendPlayerPermissionLevel(player);
        }
        player.setGameMode(mode);
        Vec3 pos = helper.absoluteVec(new Vec3(x + 0.5, y, z + 0.5));
        player.moveTo(pos.x, pos.y, pos.z, 0, 0);
        channel.outboundMessages().clear();
        return new Mock(player, channel);
    }

    /** Logs a mock player out (and removes its op entry). */
    public static void remove(GameTestHelper helper, Mock... mocks) {
        for (Mock m : mocks) {
            helper.getLevel().getServer().getPlayerList().getOps().remove(m.player().getGameProfile());
            helper.getLevel().getServer().getPlayerList().remove(m.player());
        }
    }

    /** Sends msg to the server exactly as a (modified) client would: Forge's encode, then the connection's dispatch. */
    public static void fromClient(ServerPlayer player, Object msg) {
        Packet<?> packet = ModNetwork.CHANNEL.toVanillaPacket(msg, NetworkDirection.PLAY_TO_SERVER);
        player.connection.handleCustomPayload((ServerboundCustomPayloadPacket) packet);
    }

    /** The chat / action-bar texts the server has sent this mock player since it was made (or last cleared). */
    public static List<String> chat(Mock mock) {
        List<String> texts = new ArrayList<>();
        for (Object o : mock.channel().outboundMessages()) {
            if (o instanceof ClientboundSystemChatPacket chat) texts.add(chat.content().getString());
        }
        return texts;
    }

    /** Finishes a test: succeeds when the behaviour was correct (NOT REPRODUCED), fails when the bug showed (REPRODUCED). */
    public static void result(GameTestHelper helper, String id, boolean correct, String whatHappened) {
        if (correct) {
            BetterSurvival.LOGGER.info("[BSREPRO] {} NOT REPRODUCED: {}", id, whatHappened);
            helper.succeed();
        } else {
            BetterSurvival.LOGGER.info("[BSREPRO] {} REPRODUCED: {}", id, whatHappened);
            helper.fail("REPRODUCED " + id + ": " + whatHappened);
        }
    }
}
