package moe.ku6.rollbit.server.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.util.Util;
import org.apache.commons.lang3.NotImplementedException;

import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@ToString
@Builder
@AllArgsConstructor
public class PacketHeader {
    public static final int MAGIC = 0x7940;

    private PacketType type;
    private int version;
    private long length;
    private long bodyLength;
    private long requestId;
    private int statusCode;
    private String userId;

    public PacketHeader(ByteBuf buf) {
        buf.resetReaderIndex();
        // skip 2 bytes magic
        buf.skipBytes(2);

        // 2 byte version string
        version = buf.readUnsignedShort();
        // 2 byte command id
        type = PacketType.GetById(buf.readUnsignedShort());
        // 4 byte content length
        length = buf.readUnsignedInt();
        bodyLength = length - 32;

        // 2 byte result (skip)
        statusCode = buf.readUnsignedShort();

        // 4 byte request id
        requestId = buf.readUnsignedInt();

        // 16 byte string user id
        userId = Util.ReadToStringNullTerminated(buf, 16);
    }

    public ByteBuf Serialize() {
        var ret = Unpooled.copiedBuffer(new byte[32]);
        ret.writerIndex(0);

        ret.writeShort(MAGIC);
        ret.writeShort(version);
        ret.writeShort(type.getId());
        ret.writeInt((int) length);
        ret.writeShort(statusCode);
        ret.writeInt((int) requestId);

        ret.writeBytes(userId.getBytes(StandardCharsets.UTF_8));
        ret.writeByte(0);

        ret.readerIndex(0);
        ret.writerIndex(ret.capacity());

        return ret.copy();
    }

}
