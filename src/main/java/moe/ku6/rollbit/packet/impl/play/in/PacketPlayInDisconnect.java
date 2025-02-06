package moe.ku6.rollbit.packet.impl.play.in;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;
import moe.ku6.rollbit.util.Util;

import java.util.Arrays;

@ToString
@Getter
public class PacketPlayInDisconnect extends ServerboundPacket {
    private final Reason reason;
    private final String remarks;

    public PacketPlayInDisconnect(RawPacket rawPacket) {
        super(rawPacket);
        reason = Reason.GetByCode(rawPacket.getBody().readUnsignedShort());
        remarks = Util.ReadToStringNullTerminated(rawPacket.getBody(), rawPacket.getBody().readableBytes());
    }

    @Override
    public ClientboundPacket GetResponsePacket() {
        return null;
    }

    @Override
    public PacketType GetType() {
        return PacketType.PLAY_IN_DISCONNECT;
    }

    @AllArgsConstructor
    @Getter
    public enum Reason {
        UNKNOWN(0),
        BACK_TO_LOBBY(1),
        CONNECTION_RESET(2),
        PROTOCOL_ERROR(3),
        CLIENT_EXCEPTION(4),

        ;private final int code;

        public static Reason GetByCode(int code) {
            return Arrays.stream(values())
                    .filter(v -> v.code == code)
                    .findFirst()
                    .orElse(null);
        }
    }
}
