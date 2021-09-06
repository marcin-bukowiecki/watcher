package com.watcher.api.ws;

import com.watcher.WatcherContext;
import com.watcher.messages.BreakpointStatusMessage;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class BreakpointStatusWebSocketConnection {

    private final WatcherContext watcherContext;

    private final ServerWebSocket webSocket;

    public BreakpointStatusWebSocketConnection(WatcherContext watcherContext, ServerWebSocket webSocket) {
        this.webSocket = webSocket;
        this.watcherContext = watcherContext;
        init();
    }

    private void init() {
        if (WatcherContext.getInstance().logEnabled) {
            log.info("Adding handler for {} topic", "ws.publish.breakpoint.status");
        }

        var messageConsumer = this.watcherContext.getVertx().eventBus().consumer("ws.publish.breakpoint.status", (Handler<Message<BreakpointStatusMessage>>) event -> {
            if (!webSocket.isClosed()) {
                BreakpointStatusMessage body = event.body();
                if (WatcherContext.getInstance().printASM) {
                    log.info("Got {} message for websocket publishing", body);
                }
                String s = JsonObject.mapFrom(body).toString();
                webSocket.writeTextMessage(s);
            }
        });

        webSocket.exceptionHandler(t -> {
            if (WatcherContext.getInstance().logEnabled) {
                log.info("Breakpoint status socket closed by exception", t);
            }
            messageConsumer.unregister();
        });

        webSocket.endHandler(event -> {
            if (WatcherContext.getInstance().logEnabled) {
                log.info("Breakpoint status socket closed from {}", webSocket.remoteAddress());
            }
            messageConsumer.unregister();
        });
    }
}
