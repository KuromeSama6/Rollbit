package moe.ku6.rollbit.config;

import com.beust.jcommander.Parameter;
import lombok.Getter;

@Getter
public class RollbitConfiguration {
    @Parameter(names = {"--port", "-p"}, description = "Port to listen on")
    private int port = 7940;

    @Parameter(names = {"--aes-key"}, description = "The AES key to encrypt/decrypt packets")
    private String encryptionKey;

    @Parameter(names = {"--version", "-v"}, description = "The version of the server")
    private int protocolVersion = 0x0001;

    @Parameter(names = {"--handshake-timeout"}, description = "The timeout for the handshake")
    private int handshakeTimeout = 5000;

    @Parameter(names = {"--heartbeat-interval"}, description = "The interval between heartbeats")
    private int heartbeatInterval = 3000;

    @Parameter(names = {"--max-players"}, description = "The maximum amount of players that can be connected")
    private int maxPlayers = 2;

    @Parameter(names = {"--max-spectators"}, description = "The maximum amount of spectators that can be connected")
    private int maxSpectators = 10;
}
