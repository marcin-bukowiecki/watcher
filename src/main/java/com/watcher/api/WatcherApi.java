package com.watcher.api;

import com.watcher.WatcherContext;
import com.watcher.api.dto.DebugRequest;
import com.watcher.api.handler.HelloHandler;
import com.watcher.api.ws.BreakpointStatusWebSocketConnection;
import com.watcher.api.ws.DebugDataWebSocketConnection;
import com.watcher.api.ws.HealthWebSocketConnection;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.model.Breakpoint;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Http and Web Socket Watcher API
 */
@Slf4j
public class WatcherApi {

    /**
     * Initialize the Watcher API called from {@link com.watcher.agent.WatcherAgent}
     *
     * @param watcherContext given context
     */
    public static void init(WatcherContext watcherContext) {
        log.info("Initializing Watcher API...");

        HttpServer httpServer = watcherContext.getVertx().createHttpServer();

        Router router = Router.router(watcherContext.getVertx());
        BodyHandler bodyHandler = BodyHandler.create();
        router.route().handler(bodyHandler);

        router.get("/watcher/check").handler(new HelloHandler(watcherContext));

        router.post("/watcher/debug").consumes("application/json").handler(routingContext -> {
            if (!watcherContext.getSecurityProvider().authorize(routingContext.request())) {
                log.info("Unauthorized connection from: {}", routingContext.request().remoteAddress());
                routingContext.response().setStatusCode(403).end();
                return;
            }

            HttpServerResponse response = routingContext.response();

            JsonObject bodyAsJson = routingContext.getBodyAsJson();
            var request = bodyAsJson.mapTo(DebugRequest.class);
            if (StringUtils.isEmpty(request.getBasePackages())) {
                response.write("basePackages is required");
                response.setStatusCode(400);
                response.end();
                return;
            }
            var debugSessionMessage = new DebugSessionMessage(request.getDebugSessionStatus(), request.getBasePackages());
            watcherContext.getVertx().eventBus().publish("debug.session", debugSessionMessage);

            response.setStatusCode(200);
            response.end();
        });

        router.post("/watcher/breakpoint").consumes("application/json").handler(routingContext -> {
            if (!watcherContext.getSecurityProvider().authorize(routingContext.request())) {
                log.info("Unauthorized connection from: {}", routingContext.request().remoteAddress());
                routingContext.response().setStatusCode(403).end();
                return;
            }

            if (WatcherContext.logEnabled()) {
                log.info("Handling request to set breakpoint");
            }

            JsonObject bodyAsJson = routingContext.getBodyAsJson();
            SetBreakpointRequest setBreakpointRequest = bodyAsJson.mapTo(SetBreakpointRequest.class);

            if (WatcherContext.logEnabled()) {
                log.info("Got request to set breakpoint: {}", setBreakpointRequest);
            }

            watcherContext.getWatcherSession().setBreakpoint(Breakpoint.builder()
                    .classCanonicalName(setBreakpointRequest.getClassCanonicalName())
                    .line(setBreakpointRequest.getLineNumber())
                    .build());

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(200);
            response.end();
        });

        router.delete("/watcher/breakpoint").consumes("application/json").handler(routingContext -> {
            if (!watcherContext.getSecurityProvider().authorize(routingContext.request())) {
                log.info("Unauthorized connection from: {}", routingContext.request().remoteAddress());
                routingContext.response().setStatusCode(403).end();
                return;
            }

            JsonObject bodyAsJson = routingContext.getBodyAsJson();
            var removeBreakpointRequest = bodyAsJson.mapTo(RemoveBreakpointRequest.class);

            if (WatcherContext.logEnabled()) {
                log.info("Got request to remove breakpoint: {}", removeBreakpointRequest);
            }

            watcherContext.getWatcherSession().removeBreakpoint(Breakpoint.builder()
                    .classCanonicalName(removeBreakpointRequest.getClassCanonicalName())
                    .line(removeBreakpointRequest.getLineNumber())
                    .build());

            HttpServerResponse response = routingContext.response();
            response.setStatusCode(202);
            response.end();
        });

        httpServer.webSocketHandler(webSocket -> {
            if (!watcherContext.getSecurityProvider().authorize(webSocket)) {
                log.info("Unauthorized connection from {}", webSocket.remoteAddress().host());
                webSocket.reject(403);
                return;
            }

            if (WatcherContext.logEnabled()) {
                log.info("Connection from {} at web socket", webSocket.remoteAddress());
            }

            switch (webSocket.path()) {
                case "/breakpoint/status":
                    webSocket.accept();
                    log.info("Web socket connection accepted");
                    new BreakpointStatusWebSocketConnection(watcherContext, webSocket);
                    break;
                case "/watcher/health":
                    webSocket.accept();
                    log.info("Web socket connection accepted for health check");
                    new HealthWebSocketConnection(watcherContext, webSocket);
                    break;
                case "/debug/data":
                    webSocket.accept();
                    log.info("Web socket connection accepted for debug data");
                    new DebugDataWebSocketConnection(watcherContext, webSocket);
                    break;
                default:
                    webSocket.reject(); //end
                    break;
            }
        });

        httpServer.requestHandler(router);
        httpServer.listen(watcherContext.getPort(), "localhost");

        log.info("Watcher API started at port: " + httpServer.actualPort());
    }
}
