package moe.ku6.rollbit.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.connection.DisconnectionReason;
import moe.ku6.rollbit.connection.PlayerRole;
import moe.ku6.rollbit.handler.HandlerContext;
import moe.ku6.rollbit.handler.IPacketHandler;
import moe.ku6.rollbit.handler.PacketHandler;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInAcceptMatch;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutRoomStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Arena implements IPacketHandler {
    @Getter
    private static Arena instance;

    @Getter
    private RoomStatus status = RoomStatus.STANDBY;

    private final List<Player> allPlayers = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final List<Player> spectators = new ArrayList<>();

    private Arena() {
        if (instance != null) {
            throw new IllegalStateException("Arena is already initialized");
        }
        instance = this;
        var config = Rollbit.getInstance().getConfig();
        log.info("Arena created");
        log.debug("Max players: {}", config.getMaxPlayers());
        log.debug("Max spectators: {}", config.getMaxSpectators());
    }

    public boolean AddPlayer(Player player) {
        var role = player.getRole();
        var config = Rollbit.getInstance().getConfig();
        if (role == PlayerRole.PLAYER && players.size() >= config.getMaxPlayers()) {
            return false;
        } else if (role == PlayerRole.SPECTATOR && spectators.size() >= config.getMaxSpectators()) {
            return false;
        }

        allPlayers.add(player);
        if (role == PlayerRole.PLAYER) {
            players.add(player);
            // update status
            if (players.size() == config.getMaxPlayers()) {
                status = RoomStatus.WAIT_ACCEPT;
                log.info("Enough players joined, room status updated to WAIT_ACCEPT");
            }

        } else {
            spectators.add(player);
        }

        player.getConnection().AddPacketHandler(this);
        SyncRoomStatus();
        return true;
    }

    public void RemovePlayer(Player player) {
        allPlayers.remove(player);
        players.remove(player);
        spectators.remove(player);

        if (players.size() < Rollbit.getInstance().getConfig().getMaxPlayers()) {
            status = RoomStatus.STANDBY;
            log.info("Not enough players, room status updated to STANDBY");
        }

        SyncRoomStatus();
    }

    public void SyncRoomStatus() {
        var packet = new PacketPlayOutRoomStatus(status, players.size(), spectators.size());
        for (var player : players) {
            player.getConnection().SendPacket(packet);
        }
    }


    @PacketHandler
    private void OnMatchAccept(PacketPlayInAcceptMatch packet, HandlerContext ctx) {
        if (status != RoomStatus.WAIT_ACCEPT) return;

        var player = ctx.getConnection().getPlayer();
        log.info("Player send match acceptance: {}", packet);
        if (packet.isAccepted()) {
            ctx.getConnection().getPlayer().setMatchAccepted(true);
            if (players.stream().allMatch(Player::isMatchAccepted)) {
                log.info("All players accepted the match, starting game");

                status = RoomStatus.CHARACTER_SELECT;
                SyncRoomStatus();
            }

        } else {
            log.warn("Match was rejected. Resetting server and arena.");
            new ArrayList<>(players).forEach(c -> c.getConnection().Disconnect(DisconnectionReason.MATCH_REJECTED, "match rejected by %s".formatted(player.getUid())));
            Reset();
        }
    }

    private void Destroy() {
        log.warn("Arena destroyed");
    }

    public static void Reset() {
        if (instance != null) {
            instance.Destroy();
            instance = null;
        }

        instance = new Arena();
    }
}
