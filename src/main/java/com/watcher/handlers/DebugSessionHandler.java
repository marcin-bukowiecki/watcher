package com.watcher.handlers;

import com.google.common.collect.Sets;
import com.watcher.DebugSessionStatus;
import com.watcher.Status;
import com.watcher.WatcherContext;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.model.WatcherSession;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class DebugSessionHandler extends AbstractDebugHandler implements Handler<Message<DebugSessionMessage>> {

    private final WatcherContext watcherContext;

    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    private final CompletableFuture<Void> completed = CompletableFuture.runAsync(() -> {}, pool);

    private CompletableFuture<Void> task;

    public DebugSessionHandler(WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    @Override
    public void handle(Message<DebugSessionMessage> event) {
        handle(event.body());
    }

    public CompletableFuture<Void> handle(DebugSessionMessage debugSessionMessage) {
        if (WatcherContext.logEnabled()) {
            log.info("Handling {} message", debugSessionMessage);
        }

        final WatcherSession watcherSession = this.watcherContext.getWatcherSession();

        if (watcherSession.getDebugSessionStatus() == DebugSessionStatus.ON &&
                debugSessionMessage.getDebugSessionStatus() == DebugSessionStatus.ON) {

            if (WatcherContext.logEnabled()) {
                log.info("Debug session is already running");
            }

            return completed;
        }

        Set<String> basePackages;
        if (debugSessionMessage.getDebugSessionStatus() == DebugSessionStatus.ON) {
            log.info("Starting debug session");
            watcherSession.setDebugSessionStatus(DebugSessionStatus.ON);
            watcherSession.setStatus(Status.addingBreakpointsToLoadedClasses);
            basePackages = Set.of(debugSessionMessage.getBasePackages());
        } else if (debugSessionMessage.getDebugSessionStatus() == DebugSessionStatus.OFF) {
            log.info("Stopping debug session");
            watcherSession.setDebugSessionStatus(DebugSessionStatus.OFF);
            watcherSession.setStatus(Status.removingBreakpointsFromLoadedClasses);
            basePackages = this.watcherContext.getWatcherSession().getTransformContext().getSupportedPackages();
        } else {
            throw new IllegalArgumentException("Unsupported debug session status: "
                    + (debugSessionMessage.getDebugSessionStatus() == null ? "null" :
                    debugSessionMessage.getDebugSessionStatus().name()));
        }

        if (this.task != null && !this.task.isDone()) {
            if (WatcherContext.logEnabled()) {
                log.info("Debug session process already running. Resending {} message", debugSessionMessage);
            }
            watcherContext.getVertx().eventBus().send("debug.session", debugSessionMessage);
            return completed;
        }

        this.watcherContext.getWatcherSession().getTransformContext().setSupportedPackages(Sets.newHashSet(basePackages));

        this.task = CompletableFuture.runAsync(() -> {
            if (WatcherContext.logEnabled()) {
                log.info("Instrumenting classes for debug session");
            }
            Instrumentation instrumentation = this.watcherContext.getInstrumentation();
            transform(instrumentation, watcherSession);
            watcherSession.setStatus(Status.idle);
        }, pool);

        return this.task;
    }
}
