package moe.ku6.rollbit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationEntry {
    public static void main(String[] args) {
        log.info("Starting Rollbit...");

        new Rollbit(args);
    }
}
