package moe.ku6.rollbit.packet.impl.play.in;

import lombok.Getter;
import lombok.ToString;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutGenericResponse;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;

@ToString
public class PacketPlayInAcceptMatch extends ServerboundPacket {
    @Getter
    private final boolean accepted;

    public PacketPlayInAcceptMatch(RawPacket rawPacket) {
        super(rawPacket);
        accepted = rawPacket.getBody().readByte() == 0;
    }

    @Override
    public ClientboundPacket GetResponsePacket() {
        return new PacketPlayOutGenericResponse(this, accepted ? 0 : 1);
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_IN_ACCEPT_MATCH;
    }
}
