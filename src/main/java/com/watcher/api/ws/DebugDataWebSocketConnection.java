package com.watcher.api.ws;

import com.watcher.DebugSessionStatus;
import com.watcher.WatcherContext;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.model.BreakpointData;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class DebugDataWebSocketConnection {

    private final WatcherContext watcherContext;

    private final ServerWebSocket webSocket;

    public DebugDataWebSocketConnection(final WatcherContext watcherContext, final ServerWebSocket webSocket) {
        this.watcherContext = watcherContext;
        this.webSocket = webSocket;
        init();
    }

    private void init() {
        var messageConsumer = this.watcherContext.getVertx().eventBus().consumer("debug.data", (Handler<Message<BreakpointData>>) event -> {
            var body = event.body();
            log.info("Got {} message for websocket publishing", body);
            if (!webSocket.isClosed()) {
                webSocket.writeTextMessage(JsonObject.mapFrom(body).toString());
            }
        });

        webSocket.exceptionHandler(t -> {
            log.info("Debug data socket closed by exception", t);
            stopSession();
            messageConsumer.unregister();
        });

        webSocket.endHandler(event -> {
            log.info("Debug data socket closed from {}", webSocket.remoteAddress());
            stopSession();
            messageConsumer.unregister();
        });
    }

    private void stopSession() {
        var debugSessionMessage = new DebugSessionMessage(DebugSessionStatus.OFF, "");
        watcherContext.getVertx().eventBus().publish("debug.session", debugSessionMessage);
    }
}
