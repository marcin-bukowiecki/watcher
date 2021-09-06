package com.watcher.model;

import com.google.common.annotations.VisibleForTesting;
import com.watcher.DebugSessionStatus;
import com.watcher.Status;
import com.watcher.WatcherContext;
import com.watcher.context.TransformContext;
import com.watcher.messages.RemoveBreakpointMessage;
import com.watcher.messages.SetBreakpointMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class WatcherSession {

    private final ReentrantLock sessionLock = new ReentrantLock();

    private volatile boolean collectExceptionData = Boolean.getBoolean("watcher.data.collect");

    private final boolean forceDumpOnException = Boolean.getBoolean("watcher.exception.dump.force");

    private final WatcherContext watcherContext;

    private final TransformContext transformContext = new TransformContext();

    private volatile DebugSessionStatus debugSessionStatus = DebugSessionStatus.OFF;

    private volatile Status status = Status.idle;

    public WatcherSession(final WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    public DebugSessionStatus getDebugSessionStatus() {
        return debugSessionStatus;
    }

    public boolean isCollectingData() {
        return collectExceptionData;
    }

    public void setDebugSessionStatus(DebugSessionStatus debugSessionStatus) {
        if (WatcherContext.logEnabled()) {
            System.out.println("Setting debug session to: " + debugSessionStatus);
        }
        this.debugSessionStatus = debugSessionStatus;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setBreakpoint(final Breakpoint breakpoint) {
        this.watcherContext.getVertx().eventBus().publish("breakpoint.set", new SetBreakpointMessage(breakpoint));
    }

    public void removeBreakpoint(final Breakpoint breakpoint) {
        this.watcherContext.getVertx().eventBus().publish("breakpoint.remove", new RemoveBreakpointMessage(breakpoint));
    }

    public boolean isDebugging() {
        return debugSessionStatus == DebugSessionStatus.ON;
    }

    public boolean isLocked() {
        return sessionLock.isLocked();
    }

    public void lock() {
        sessionLock.lock();
    }

    public void unlock() {
        sessionLock.unlock();
    }

    public boolean tryLock() {
        return sessionLock.tryLock();
    }

    public boolean canAppendDebugger(Class<?> aClass) {
        String canonicalName = aClass.getCanonicalName();
        if (StringUtils.isEmpty(canonicalName)) {
            return false;
        }
        return canonicalName.startsWith("sandbox");
    }

    @VisibleForTesting
    public void setCollectExceptionData(boolean collectExceptionData) {
        this.collectExceptionData = collectExceptionData;
    }

    public boolean isForceDumpOnException() {
        return forceDumpOnException;
    }

    public TransformContext getTransformContext() {
        return transformContext;
    }
}
