package com.watcher.security;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;

import java.util.Set;

/**
 * Security provider which will authorize requests only for known hosts
 *
 * @author Marcin Bukowiecki
 */
public class KnownHostsSecurityProvider implements SecurityProvider {

    private final Set<String> knownHosts;

    public KnownHostsSecurityProvider(Set<String> knownHosts) {
        this.knownHosts = knownHosts;
    }

    @Override
    public boolean authorize(ServerWebSocket webSocket) {
        return knownHosts.contains(webSocket.remoteAddress().host());
    }

    @Override
    public String info() {
        return "known hosts";
    }

    @Override
    public boolean authorize(HttpServerRequest request) {
        return knownHosts.contains(request.remoteAddress().host());
    }
}
