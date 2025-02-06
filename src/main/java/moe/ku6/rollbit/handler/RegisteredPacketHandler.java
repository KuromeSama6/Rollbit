package moe.ku6.rollbit.handler;

import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.server.Server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RegisteredPacketHandler {
    private final IPacketHandler handler;
    private final Map<Class<? extends ServerboundPacket>, List<Method>> methods = new HashMap<>();

    public RegisteredPacketHandler(IPacketHandler handler) {
        this.handler = handler;

        // register methods
        for (var method : handler.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(PacketHandler.class)) {
                continue;
            }

            method.setAccessible(true);
            if (method.getParameterCount() != 2) {
                throw new IllegalArgumentException("Handler method %s must have exactly 2 paramter".formatted(method));
            }

            var type = method.getParameters()[0].getType();
            if (!ServerboundPacket.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Handler method %s must have a parameter that is a subclass of ServerboundPacket".formatted(method));
            }

            if (!HandlerContext.class.isAssignableFrom(method.getParameters()[1].getType())) {
                throw new IllegalArgumentException("Handler method %s must have a parameter that is a subclass of HandlerContext".formatted(method));
            }

            methods.computeIfAbsent((Class<? extends ServerboundPacket>)type, a -> new ArrayList<>()).add(method);

            log.debug("Added packet handler %s on %s".formatted(method, handler));
        }

    }

    public <T extends ServerboundPacket> void TryHandle(HandlerContext ctx, T packet) {
        var list = methods.get(packet.getClass());
        if (list == null) return;
        list.forEach(c -> {
            try {
                c.invoke(handler, packet, ctx);
            } catch (Exception e) {
                log.error("Error while handling packet %s".formatted(packet));
                e.printStackTrace();
            }
        });
    }

}
