package moe.ku6.rollbit.packet.impl.play.out;

import io.netty.buffer.ByteBuf;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.server.packet.PacketType;

public class PacketPlayOutGenericResponse extends ClientboundPacket {
    private final int code;
    public PacketPlayOutGenericResponse(ServerboundPacket packet, int code) {
        super(packet);
        this.code = code;
    }

    @Override
    protected void SerializeInternal(ByteBuf buf) {

    }

    @Override
    protected int GetBodySize() {
        return 0;
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_OUT_RESPONSE;
    }

    @Override
    protected int GetStatusCode() {
        return code;
    }
}
