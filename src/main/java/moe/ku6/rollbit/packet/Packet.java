package moe.ku6.rollbit.packet;

import moe.ku6.rollbit.server.packet.PacketType;

public abstract class Packet {
    protected final PacketType type;

    protected Packet() {
        this.type = GetType();
    }

    public abstract PacketType GetType();
}
