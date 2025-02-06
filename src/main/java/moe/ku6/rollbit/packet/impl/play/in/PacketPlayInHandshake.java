package moe.ku6.rollbit.packet.impl.play.in;

import lombok.Getter;
import lombok.ToString;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.connection.PlayerRole;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutHandshake;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;
import org.joda.time.DateTime;

@ToString
public class PacketPlayInHandshake extends ServerboundPacket {
    @Getter
    private final PlayerRole role;
    @Getter
    private final DateTime timestamp;

    public PacketPlayInHandshake(RawPacket rawPacket) {
        super(rawPacket);
        role = PlayerRole.values()[rawPacket.getBody().getByte(0)];
        timestamp = new DateTime(rawPacket.getBody().getLong(2));
    }

    @Override
    public ClientboundPacket GetResponsePacket() {
        return new PacketPlayOutHandshake(this);
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_IN_HANDSHAKE;
    }
}
