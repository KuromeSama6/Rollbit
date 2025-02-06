package moe.ku6.rollbit.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.util.Util;

@Slf4j
public class PacketEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf buf) throws Exception {
        var config = Rollbit.getInstance().getConfig();
        var header = msg.slice(0, 32);
        ctx.write(config.getEncryptionKey() != null ? RollbitCodec.Encrypt(header) : header);
        var body = msg.slice(32, msg.capacity() - 32);
        ctx.write(config.getEncryptionKey() != null ? RollbitCodec.Encrypt(body) : body);
        ctx.flush();
    }
}
