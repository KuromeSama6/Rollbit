package moe.ku6.rollbit.server.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.ku6.rollbit.packet.Packet;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInAcceptMatch;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInDisconnect;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInHandshake;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInHeartbeat;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutDisconnect;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutGenericResponse;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutHandshake;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutRoomStatus;

import java.util.Arrays;

/*
    Packet types for AimeDb packets
    See https://sega.bsnk.me/allnet/aimedb/packet_index/ for a list of known packet types.
 */
@AllArgsConstructor
@Getter
public enum PacketType {
    PLAY_IN_HANDSHAKE(0x01, PacketDirection.SERVERBOUND, PacketPlayInHandshake.class),
    PLAY_IN_HEARTBEAT(0x02, PacketDirection.SERVERBOUND, PacketPlayInHeartbeat.class),
    PLAY_IN_ACCEPT_MATCH(0x04, PacketDirection.SERVERBOUND, PacketPlayInAcceptMatch.class),

    PLAY_IN_DISCONNECT(0xff, PacketDirection.SERVERBOUND, PacketPlayInDisconnect.class),

    PLAY_OUT_RESPONSE(0xff00, PacketDirection.CLIENTBOUND, PacketPlayOutGenericResponse.class),
    PLAY_OUT_HANDSHAKE(0xff01, PacketDirection.CLIENTBOUND, PacketPlayOutHandshake.class),
    PLAY_OUT_ROOM_STATUS(0xff04, PacketDirection.CLIENTBOUND, PacketPlayOutRoomStatus.class),

    PLAY_OUT_DISCONNECT(0xffff, PacketDirection.CLIENTBOUND, PacketPlayOutDisconnect.class)
    ;
    private final int id;
    private final PacketDirection direction;
    private final Class<? extends Packet> packetClass;

    public static PacketType GetById(int id) {
        return Arrays.stream(values())
                .filter(v -> v.id == id)
                .findFirst()
                .orElse(null);
    }
}
