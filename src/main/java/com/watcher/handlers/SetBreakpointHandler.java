package com.watcher.handlers;

import com.watcher.DebugSessionStatus;
import com.watcher.Status;
import com.watcher.WatcherContext;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.Breakpoint;
import com.watcher.model.BreakpointStatus;
import com.watcher.model.WatcherSession;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class SetBreakpointHandler extends AbstractDebugHandler implements Handler<Message<SetBreakpointMessage>> {

    private final WatcherContext watcherContext;

    private final ExecutorService pool = Executors.newFixedThreadPool(1);

    private final CompletableFuture<Void> completed = CompletableFuture.runAsync(() -> {}, pool);

    private CompletableFuture<Void> task;

    public SetBreakpointHandler(WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    @Override
    public void handle(Message<SetBreakpointMessage> event) {
        handle(event.body());
    }

    public CompletableFuture<Void> handle(SetBreakpointMessage setBreakpointMessage) {
        if (WatcherContext.logEnabled()) {
            log.info("Handling {} message", setBreakpointMessage);
        }

        final WatcherSession watcherSession = this.watcherContext.getWatcherSession();
        final Breakpoint breakpoint = setBreakpointMessage.getBreakpoint();
        final String classCanonicalName = breakpoint.getClassCanonicalName();

        if (watcherSession.getDebugSessionStatus() == DebugSessionStatus.OFF) {
            if (WatcherContext.logEnabled()) {
                log.info("Debug session is not active. Can't set breakpoint {}", breakpoint);
            }

            var loadedClassContext = watcherContext.getOrCreateLoadedClassContext(classCanonicalName);
            loadedClassContext.lock();
            loadedClassContext.addBreakpoint(breakpoint);
            loadedClassContext.unlock();
            breakpoint.setStatus(BreakpointStatus.inactive);
            return completed;
        }

        if (watcherSession.getStatus() != Status.idle) {
            if (WatcherContext.logEnabled()) {
                log.info("Resending message because Watcher Agent is not idle");
            }
            resend(setBreakpointMessage);
            return completed;
        }

        var loadedClassContext = watcherContext.getOrCreateLoadedClassContext(classCanonicalName);
        loadedClassContext.lock();
        boolean existed = loadedClassContext.addBreakpoint(breakpoint);
        loadedClassContext.unlock();

        if (existed) {
            if (WatcherContext.logEnabled()) {
                log.info("Breakpoint {} already exists", breakpoint);
            }
            return completed;
        }

        if (loadedClassContext.version() == 0) {
            if (WatcherContext.logEnabled()) {
                log.info("Breakpoint {} ready to set because class {} is not loaded yet", breakpoint, classCanonicalName);
            }
            return completed;
        }

        if (this.task != null && !this.task.isDone()) {
            resend(setBreakpointMessage);
        }

        this.task = CompletableFuture.runAsync(() -> {
            var instrumentation = this.watcherContext.getInstrumentation();
            transform(instrumentation, watcherSession);
            watcherSession.setStatus(Status.idle);
        }, pool);

        return this.task;
    }

    private void resend(SetBreakpointMessage setBreakpointMessage) {
        this.watcherContext.getVertx().eventBus().publish("breakpoint.set", setBreakpointMessage);
    }
}
