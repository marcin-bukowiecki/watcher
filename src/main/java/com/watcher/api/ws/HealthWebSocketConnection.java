package com.watcher.api.ws;

import com.watcher.LoadedClassContext;
import com.watcher.WatcherContext;
import com.watcher.messages.RemoveBreakpointMessage;
import com.watcher.model.Breakpoint;
import io.vertx.core.http.ServerWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class HealthWebSocketConnection {

    private final WatcherContext watcherContext;

    private final ServerWebSocket webSocket;

    public HealthWebSocketConnection(final WatcherContext watcherContext, final ServerWebSocket webSocket) {
        this.watcherContext = watcherContext;
        this.webSocket = webSocket;
        init();
    }

    private void init() {
        webSocket.endHandler(event -> {
            log.info("Health socket closed from {}", webSocket.remoteAddress());
        });
        webSocket.exceptionHandler(event -> {
            log.info("Health socket closed on exception from {}", webSocket.remoteAddress(), event.getCause());
        });
        webSocket.closeHandler(e -> {
            log.info("Socket closed removing all breakpoints");
            for (Map.Entry<String, LoadedClassContext> entry : watcherContext.getLoadedClassContext().entrySet()) {
                LoadedClassContext value = entry.getValue();
                for (Breakpoint copyBreakpoint : value.copyBreakpoints()) {
                    watcherContext.getVertx().eventBus().publish("breakpoint.remove", new RemoveBreakpointMessage(copyBreakpoint));
                }
            }
        });
    }
}
