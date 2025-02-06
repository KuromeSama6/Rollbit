package moe.ku6.rollbit.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.connection.DisconnectionReason;
import moe.ku6.rollbit.server.packet.RawPacket;
import moe.ku6.rollbit.server.packet.PacketHeader;
import moe.ku6.rollbit.util.Util;

import java.util.List;

@Slf4j
public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        var handler = (Connection) ctx.channel().pipeline().get("handler");
//        log.info("client read, {}, readable {}, writable {}", buf, buf.readableBytes(), buf.writableBytes());
        if (buf.readableBytes() < 32) {
            log.warn("invalid packet size. Expected at least 32 bytes, but got {}", buf.readableBytes());
            handler.Disconnect(DisconnectionReason.MALFORMED_PACKET, "invalid header size");
            return;
        }

        if (buf.readableBytes() % 16 != 0) {
            log.warn("invalid packet size. Expected multiple of 16 bytes, but got {}", buf.readableBytes());
            handler.Disconnect(DisconnectionReason.MALFORMED_PACKET, "invalid packet size");
            return;
        }

        // decrypt
        var config = Rollbit.getInstance().getConfig();
        var decrypted = config.getEncryptionKey() != null ? RollbitCodec.Decrypt(buf) : buf;

        log.debug("Packet data: {}", Util.FormatByteBuf(decrypted));

        var magic = decrypted.getUnsignedShort(0);
        if (magic != PacketHeader.MAGIC) {
            log.warn("invalid magic. Expected %02X, but got %02X".formatted(PacketHeader.MAGIC, magic));
            handler.Disconnect(DisconnectionReason.MALFORMED_PACKET, "invalid magic");
            return;
        }

        var length = decrypted.getUnsignedInt(6);
        var packet = new RawPacket(decrypted.slice(0, (int)length));
        log.debug("pkt header: {}", packet.getHeader());

        if (packet.getHeader().getLength() != length) {
            log.warn("invalid packet size. Reported {}, but got {}", packet.getHeader().getLength(), decrypted.capacity());
            handler.Disconnect(DisconnectionReason.MALFORMED_PACKET, "invalid packet size");
            return;
        }

        list.add(packet);
        // move buf reader index to the end
        buf.readerIndex(buf.writerIndex());
    }
}
