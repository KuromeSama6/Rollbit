package moe.ku6.rollbit.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.config.RollbitConfiguration;
import moe.ku6.rollbit.connection.Connection;
import moe.ku6.rollbit.server.codec.PacketDecoder;
import moe.ku6.rollbit.server.codec.PacketEncoder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class Server {
    private final RollbitConfiguration config;
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Getter
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public Server(RollbitConfiguration config) {
        this.config = config;
        if (config.getEncryptionKey() != null) log.info("Encryption key: {}", config.getEncryptionKey());
        else log.info("No encryption key set");

        StartServer();
    }

    private void StartServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast("encoder", new PacketEncoder())
                                .addLast("decoder", new PacketDecoder())
                                .addLast("handler", new Connection(Server.this, ch, executorService));
                    }
                });

        var port = config.getPort();
        bootstrap.bind(port)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Server started on port {}", port);
                    } else {
                        log.error("Failed to start server on port {}", port);
                    }
                });


        Runtime.getRuntime().addShutdownHook(new Thread(this::Shutdown));
    }

    private void Shutdown() {
        log.info("Shutting down server");

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        executorService.shutdown();
        log.info("Server shutdown complete");
    }
}
