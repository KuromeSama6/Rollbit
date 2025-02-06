package moe.ku6.rollbit.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.Rollbit;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RollbitCodec {

    public static ByteBuf Decrypt(ByteBuf buf) {
        try {
            var cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, GetKey());
            return Unpooled.copiedBuffer(cipher.doFinal(ByteBufUtil.getBytes(buf)));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuf Encrypt(ByteBuf buf) {
        try {
            var cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, GetKey());
            buf.readerIndex(0);
            buf.writerIndex(buf.capacity());
            return Unpooled.copiedBuffer(cipher.doFinal(ByteBufUtil.getBytes(buf)));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static SecretKeySpec GetKey() {
        return new SecretKeySpec(Rollbit.getInstance().getConfig().getEncryptionKey().getBytes(StandardCharsets.UTF_8), "AES");
    }
}
