package moe.ku6.rollbit.packet;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.server.packet.PacketDirection;
import moe.ku6.rollbit.server.packet.PacketHeader;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public abstract class ServerboundPacket extends Packet {
    @Getter
    private final PacketHeader header;
    private final ByteBuf payload;

    public ServerboundPacket(RawPacket rawPacket) {
        if (rawPacket.getType() != GetType()) {
            throw new IllegalArgumentException("Could not create packet %s from raw packet type %s".formatted(GetType(), rawPacket.getType()));
        }

        if (GetType().getDirection() == PacketDirection.CLIENTBOUND) {
            throw new IllegalArgumentException("Cannot create a clientbound packet from a raw packet");
        }

        header = rawPacket.getHeader();
        payload = rawPacket.getBody().copy();
    }

    public abstract ClientboundPacket GetResponsePacket();

    public static <T extends ServerboundPacket> T NewInstance(Class<T> clazz, RawPacket packet) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        log.info(clazz.getName());
        return (T) clazz.getConstructor(RawPacket.class).newInstance(packet);
    }
}
