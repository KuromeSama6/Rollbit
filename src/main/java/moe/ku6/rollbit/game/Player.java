package moe.ku6.rollbit.game;

import lombok.Getter;
import lombok.Setter;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.connection.PlayerRole;

public class Player {
    @Getter
    private final String uid;
    @Getter
    private final PlayerRole role;
    @Getter
    private final Connection connection;

    @Getter
    private boolean online = true;
    @Getter
    private Arena arena;
    @Getter @Setter
    private boolean matchAccepted;

    public Player(String uid, Connection connection, PlayerRole role) {
        this.uid = uid;
        this.connection = connection;
        this.role = role;

        connection.getScheduler().Add(this::JoinArena, 250);
    }

    private void JoinArena() {
        arena = Arena.getInstance();
        arena.AddPlayer(this);
    }

    public void Destroy() {
        if (!online) return;

        if (arena != null) {
            arena.RemovePlayer(this);
        }
        online = false;
    }

}
