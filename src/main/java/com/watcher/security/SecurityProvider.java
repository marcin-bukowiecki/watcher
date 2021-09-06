package com.watcher.security;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;

/**
 * @author Marcin Bukowiecki
 */
public interface SecurityProvider {
    boolean authorize(ServerWebSocket webSocket);

    String info();

    boolean authorize(HttpServerRequest request);
}
