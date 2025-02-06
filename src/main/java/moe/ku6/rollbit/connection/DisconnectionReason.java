package moe.ku6.rollbit.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DisconnectionReason {
    CLIENT_DISCONNECT(0),
    INTERNAL_ERROR(1),
    MALFORMED_PACKET(2),
    NOT_FOUND(3),
    VERSION_MISMATCH(4),
    INVALID_HEADERS(5),
    LENGTH_MISMATCH(6),
    HANDSHAKE_TIMEOUT(7),
    TIMEOUT(8),
    SEE_REMARKS(15),
    PERMISSION_DENIED(16),

    ROOM_FULL(20),
    MATCH_REJECTED(21)

    ;private final int code;
}
