package moe.ku6.rollbit.server.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class RawPacket {
    private final PacketType type;
    private final PacketHeader header;
    private final ByteBuf body;

    public RawPacket(ByteBuf buf) {
        // first 32 bytes
        header = new PacketHeader(buf.copy().slice(0, 32));
        type = header.getType();
        body = buf.copy().slice(32, (int)header.getBodyLength());
    }
}
