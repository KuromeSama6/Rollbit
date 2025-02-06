package moe.ku6.rollbit;

import com.beust.jcommander.JCommander;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.ku6.rollbit.config.RollbitConfiguration;
import moe.ku6.rollbit.game.Arena;
import moe.ku6.rollbit.server.Server;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.tools.ant.types.Commandline;

@Slf4j
public class Rollbit {
    @Getter
    private static Rollbit instance;
    @Getter
    private RollbitConfiguration config;
    @Getter
    private Server server;

    public Rollbit(String[] args) {
        if (instance != null) {
            throw new IllegalStateException("Rollbit is already running!");
        }
        instance = this;

        var stopwatch = new StopWatch();
        stopwatch.start();

        // parse config
        try {
            log.info("Parsing configuration...");
            config = new RollbitConfiguration();
            JCommander.newBuilder()
                    .addObject(config)
                    .build()
                    .parse(Commandline.translateCommandline(String.join(" ", args)));
        } catch (Exception e) {
            log.error("There was an error parsing the configuration!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // start server
        try {
            log.info("Starting server...");
            server = new Server(config);
            log.info("Server started!");
        } catch (Exception e) {
            log.error("There was an error starting the server!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        // create arena
        try {
            log.info("Creating arena...");
            Arena.Reset();
        } catch (Exception e) {
            log.error("There was an error creating the arena!");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        stopwatch.stop();
        log.info("Done! Took {}ms.", stopwatch.formatTime());
    }
}
