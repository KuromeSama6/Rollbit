package moe.ku6.rollbit.packet.impl.play.out;

import io.netty.buffer.ByteBuf;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.server.packet.PacketType;

public class PacketPlayOutHandshake extends ClientboundPacket {
    private final int heartbeatInterval;

    public PacketPlayOutHandshake(ServerboundPacket packet) {
        super(packet);
        heartbeatInterval = Rollbit.getInstance().getConfig().getHeartbeatInterval();
    }

    @Override
    protected void SerializeInternal(ByteBuf buf) {
        buf.writeInt(heartbeatInterval);
    }

    @Override
    protected int GetBodySize() {
        return 4;
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_OUT_HANDSHAKE;
    }
}
