package moe.ku6.rollbit.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum RoomStatus {
    STANDBY(0),
    WAIT_ACCEPT(1),
    CHARACTER_SELECT(2),
    NEGOTIATING(3),
    CLIENT_LOADING(4),
    PLAYING(5),
    FINISHED(6),

    ;private final int id;

    public static RoomStatus GetById(int id) {
        return Arrays.stream(values()).filter(x -> x.getId() == id).findFirst().orElse(null);
    }
}
