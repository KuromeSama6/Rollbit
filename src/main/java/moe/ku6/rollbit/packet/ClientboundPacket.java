package moe.ku6.rollbit.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.server.packet.PacketHeader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class ClientboundPacket extends Packet {
    private final ServerboundPacket request;
    protected final long requestId;

    public ClientboundPacket() {
        requestId = 0;
        request = null;
    }

    public ClientboundPacket(ServerboundPacket request){
        this.request = request;
        requestId = request.getHeader().getRequestId();
    }

    public ByteBuf Serialize() {
        var ret = Unpooled.copiedBuffer(new byte[GetBodySize()]);
        ret.writerIndex(0);
        SerializeInternal(ret);
        return ret.copy();
    }

    public ByteBuf GetHeaderBuffer(Connection connection) {
        var config = Rollbit.getInstance().getConfig();
        var length = GetBodySize() + 32;
        var header = PacketHeader.builder()
                .type(GetType())
                .version(config.getProtocolVersion())
                .length(length)
                .userId(connection.getPlayer().getUid())
                .requestId(requestId)
                .statusCode(GetStatusCode())
                .build();
        return header.Serialize();
    }

    protected abstract void SerializeInternal(ByteBuf buf);
    protected abstract int GetBodySize();
    protected int GetStatusCode() {
        return 0;
    }
}
