package moe.ku6.rollbit.connection;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.Rollbit;
import moe.ku6.rollbit.game.Arena;
import moe.ku6.rollbit.game.Player;
import moe.ku6.rollbit.handler.HandlerContext;
import moe.ku6.rollbit.handler.IPacketHandler;
import moe.ku6.rollbit.handler.PacketHandler;
import moe.ku6.rollbit.handler.RegisteredPacketHandler;
import moe.ku6.rollbit.packet.ClientboundPacket;
import moe.ku6.rollbit.packet.ServerboundPacket;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInAcceptMatch;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInDisconnect;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInHandshake;
import moe.ku6.rollbit.packet.impl.play.in.PacketPlayInHeartbeat;
import moe.ku6.rollbit.packet.impl.play.out.PacketPlayOutDisconnect;
import moe.ku6.rollbit.server.Server;
import moe.ku6.rollbit.server.packet.PacketType;
import moe.ku6.rollbit.server.packet.RawPacket;
import moe.ku6.rollbit.util.PooledScheduler;
import moe.ku6.rollbit.util.Util;
import org.joda.time.DateTime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class Connection extends SimpleChannelInboundHandler<RawPacket> implements IPacketHandler {
    @Getter
    private final Server server;
    @Getter
    private final SocketChannel channel;
    @Getter
    private final PooledScheduler scheduler;

    @Getter
    private boolean established = false;

    private DateTime lastHeartbeat = DateTime.now();
    private int timeoutTaskHandle;
    private final List<RegisteredPacketHandler> packetHandlers = new ArrayList<>();
    @Getter
    private Player player;

    public Connection(Server server, SocketChannel channel, ScheduledExecutorService executorService) {
        this.server = server;
        this.channel = channel;
        this.scheduler = new PooledScheduler(executorService);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        log.info("Client connected: {}", ctx.channel().remoteAddress());

        var config = Rollbit.getInstance().getConfig();
        timeoutTaskHandle = scheduler.Add(() -> {
            log.warn("Client %s handshake timed out".formatted(channel.remoteAddress()));
            Disconnect(DisconnectionReason.HANDSHAKE_TIMEOUT, "handshake timeout");
        }, config.getHandshakeTimeout());

        AddPacketHandler(this);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        scheduler.Free();
        if (player != null) {
            player.Destroy();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RawPacket packet) throws Exception {
        server.getExecutorService().submit(() -> {
            HandlePacket(packet);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.info("exception caught");
    }

    public void AddPacketHandler(IPacketHandler handler) {
        packetHandlers.add(new RegisteredPacketHandler(handler));
    }

    private void HandlePacket(RawPacket rawPacket) {
        try {
            var packet = ServerboundPacket.NewInstance((Class<? extends ServerboundPacket>)rawPacket.getType().getPacketClass(), rawPacket);

            if (!established && packet.getHeader().getType() != PacketType.PLAY_IN_HANDSHAKE) {
                Disconnect(DisconnectionReason.PERMISSION_DENIED, "not established");
                return;
            }

            var context = new HandlerContext(this);
            for (var handler : packetHandlers) {
                handler.TryHandle(context, packet);
                if (context.isCancelled()) break;
            }

            // response
            var response = packet.GetResponsePacket();
//            log.info("response: {}", response);
            if (response == null) return;

            SendPacket(response);

            // deferred tasks
            context.ExecuteDeferred();

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("Failed to instantiate packet");
            e.printStackTrace();
            Disconnect(DisconnectionReason.INTERNAL_ERROR, "error instantiating packet: %s".formatted(e.getCause()));

        } catch (Exception e) {
            log.error("Failed to handle packet");
            e.printStackTrace();
            Disconnect(DisconnectionReason.INTERNAL_ERROR, "error handling packet: %s".formatted(e.getCause()));
        }
    }

    public void SendPacket(ClientboundPacket packet) {
        var body = packet.Serialize();
        var header = packet.GetHeaderBuffer(this);
        if (header == null)
            throw new IllegalArgumentException("Could not create packet header");

        var size = header.capacity() + body.capacity();
        var padding = (16 - (size % 16)) % 16;
        var data = Unpooled.copiedBuffer(new byte[size + padding]);
        data.writerIndex(0);
        data.writeBytes(header);
        data.writeBytes(body);

        log.debug("send packet %s".formatted(packet));
//        log.debug("send: %s".formatted(Util.FormatByteBuf(data)));
        channel.writeAndFlush(data);
    }

    public void Disconnect(DisconnectionReason reason) {
        Disconnect(reason, "");
    }

    public void Disconnect(DisconnectionReason reason, String message) {
        var packet = new PacketPlayOutDisconnect(reason, message);
        log.debug("disconnect");
        scheduler.Cancel(timeoutTaskHandle);

        try {
            SendPacket(packet);
            channel.close();
            player.Destroy();
        } catch (Exception e) {
            log.error("Failed to send disconnect packet", e);
        }
    }

    //region Packet Handlers
    @PacketHandler
    private void OnHandshake(PacketPlayInHandshake packet, HandlerContext ctx) {
        log.info("Handshake: %s".formatted(packet));
        // check timestamp - within 30 seconds
        if (packet.getTimestamp().isBefore(DateTime.now().minusSeconds(30))) {
            ctx.setCancelled(true);
            Disconnect(DisconnectionReason.HANDSHAKE_TIMEOUT, "too late");
            return;
        }

        established = true;
        var userId = packet.getHeader().getUserId();
        var role = packet.getRole();
        player = new Player(userId, this, role);

        scheduler.Cancel(timeoutTaskHandle);
        log.info("Established: %s; uid: %s".formatted(channel.remoteAddress(), userId));

        // heartbeat timer
        var interval = Rollbit.getInstance().getConfig().getHeartbeatInterval();
        scheduler.AddRepeated(() -> {
            if (lastHeartbeat.plusMillis(interval).isBefore(DateTime.now())) {
                Disconnect(DisconnectionReason.TIMEOUT, "heartbeat timeout");
            }
        }, interval, interval);
    }

    @PacketHandler
    private void OnHeartbeat(PacketPlayInHeartbeat heartbeat, HandlerContext ctx) {
        log.debug("Heartbeat: %s".formatted(heartbeat));
        lastHeartbeat = DateTime.now();
    }

    @PacketHandler
    private void OnDisconnectRequest(PacketPlayInDisconnect packet, HandlerContext ctx) {
        log.warn("Client requested disconnect: {}", packet);
        Disconnect(DisconnectionReason.CLIENT_DISCONNECT, "client requested disconnect: %s".formatted(packet.getReason()));
    }
    //endregion
}
