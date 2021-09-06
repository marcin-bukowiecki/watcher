package com.watcher.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Watcher logger
 *
 * @author Marcin Bukowiecki
 */
public class WatcherLogger {

    private final Logger log;

    public WatcherLogger(Class<?> clazz) {
        this.log = LoggerFactory.getLogger(clazz);
    }

    public static WatcherLogger create(Class<?> clazz) {
        return new WatcherLogger(clazz);
    }

    public void debug(String message, Object var) {
        this.log.info(message, var);
    }

    public void debug(String message, Object var1, Object var2) {
        this.log.info(message, var1, var2);
    }

    public void info(String message, Object var1, Object var2) {
        this.log.info(message, var1, var2);
    }

    public void info(String message, Object var) {
        this.log.info(message, var);
    }
}
