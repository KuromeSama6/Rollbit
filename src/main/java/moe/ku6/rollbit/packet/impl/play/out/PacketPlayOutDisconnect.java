package moe.ku6.rollbit.packet.impl.play.out;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import moe.ku6.rollbit.connection.DisconnectionReason;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.util.Util;

@AllArgsConstructor
public class PacketPlayOutDisconnect extends ClientboundPacket {
    private final DisconnectionReason reason;
    private final String remarks;

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_OUT_DISCONNECT;
    }

    @Override
    protected int GetBodySize() {
        return 2 + remarks.length() + 1;
    }

    @Override
    protected void SerializeInternal(ByteBuf buf) {
        buf.writeShort(reason.getCode());

        Util.WriteStringNullTerminated(buf, remarks);
    }
}
