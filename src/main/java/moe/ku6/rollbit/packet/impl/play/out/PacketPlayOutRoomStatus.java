package moe.ku6.rollbit.packet.impl.play.out;

import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import moe.ku6.rollbit.game.RoomStatus;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.server.packet.PacketType;

@RequiredArgsConstructor
public class PacketPlayOutRoomStatus extends ClientboundPacket {
    private final RoomStatus status;
    private final int players;
    private final int spectators;

    @Override
    protected void SerializeInternal(ByteBuf buf) {
        buf.writeShort(status.getId());
        buf.writeShort(players);
        buf.writeShort(spectators);
    }

    @Override
    protected int GetBodySize() {
        return 6;
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_OUT_ROOM_STATUS;
    }
}
