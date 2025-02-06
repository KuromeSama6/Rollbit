package moe.ku6.rollbit.packet.impl.play.in;

import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutGenericResponse;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;
import org.joda.time.DateTime;

public class PacketPlayInHeartbeat extends ServerboundPacket {
    private final DateTime timestamp;
    public PacketPlayInHeartbeat(RawPacket rawPacket) {
        super(rawPacket);
        timestamp = new DateTime(rawPacket.getBody().getLong(0));
    }

    @Override
    public ClientboundPacket GetResponsePacket() {
        return new PacketPlayOutGenericResponse(this, 0);
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_IN_HEARTBEAT;
    }
}
