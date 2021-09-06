package com.watcher.security;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;

/**
 * Security provider which will authorize requests coming from localhost
 *
 * @author Marcin Bukowiecki
 */
public class DefaultSecurityProvider implements SecurityProvider {

    @Override
    public boolean authorize(ServerWebSocket webSocket) {
        final SocketAddress socketAddress = webSocket.remoteAddress();
        return socketAddress.host().equals("localhost")
                || socketAddress.host().equals("127.0.0.1");
    }

    @Override
    public String info() {
        return "default";
    }

    @Override
    public boolean authorize(HttpServerRequest request) {
        return request.remoteAddress().host().equals("localhost")
                || request.remoteAddress().host().equals("127.0.0.1");
    }
}
