package com.watcher.handlers;

import com.watcher.LoadedClassContext;
import com.watcher.WatcherContext;
import com.watcher.messages.RemoveBreakpointMessage;
import com.watcher.model.Breakpoint;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class RemoveBreakpointHandler implements Handler<Message<RemoveBreakpointMessage>> {

    private final WatcherContext watcherContext;

    private final CompletableFuture<Void> completed = CompletableFuture.runAsync(() -> {});

    private CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {});

    public RemoveBreakpointHandler(WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    @Override
    public void handle(Message<RemoveBreakpointMessage> event) {
        handle(event.body());
    }

    public CompletableFuture<Void> handle(RemoveBreakpointMessage message) {
        final Breakpoint breakpoint = message.getBreakpoint();
        final String classCanonicalName = breakpoint.getClassCanonicalName();

        LoadedClassContext loadedClassContext = watcherContext.getLoadedClassContext(classCanonicalName);
        loadedClassContext.lock();
        boolean existed = loadedClassContext.removeBreakpoint(breakpoint);
        loadedClassContext.unlock();

        if (!existed) {
            log.info("Couldn't remove breakpoint {} because it didn't exists", breakpoint);
            return completed;
        }

        if (!task.isDone()) {
            watcherContext.getVertx().eventBus().send("breakpoint.remove", message);
            return completed;
        }

        return (task = CompletableFuture.runAsync(() -> watcherContext.flush(classCanonicalName)));
    }
}
