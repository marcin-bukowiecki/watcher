package com.watcher.api.handler;

import com.watcher.WatcherContext;
import com.watcher.log.WatcherLogger;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.logging.Logger;

/**
 * @author Marcin Bukowiecki
 *
 * Hello handler :)
 */
public class HelloHandler implements Handler<RoutingContext> {

    private static final WatcherLogger log = WatcherLogger.create(HelloHandler.class);

    private final WatcherContext watcherContext;

    public HelloHandler(WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    /**
     * Simple vecrt hello handler :)
     *
     * @param routingContext given vertx routing context
     */
    @Override
    public void handle(RoutingContext routingContext) {
        if (!watcherContext.getSecurityProvider().authorize(routingContext.request())) {
            log.info("Unauthorized connection from: {}", routingContext.request().remoteAddress());
            routingContext.response().setStatusCode(403).end();
            return;
        }
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");
        response.end("Hello from watcher agent");
    }
}
