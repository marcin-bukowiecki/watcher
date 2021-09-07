package com.watcher.service;

import com.watcher.WatcherContext;
import com.watcher.events.*;
import com.watcher.model.BreakpointData;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.*;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public final class ThreadLocalCollector {

    public static final ThreadLocal<CollectingContext> THREAD_LOCAL = new ThreadLocal<>();

    public static final Set<CollectingContext> collectingRegister = Collections.newSetFromMap(new WeakHashMap<>());

    public static final ThreadLocalCollector INSTANCE = new ThreadLocalCollector();

    private final StackWalker stackWalker = StackWalker.getInstance();

    public void setCurrentPlace(String classCanonicalName) {
        CollectingContext collectingContext = THREAD_LOCAL.get();
        if (collectingContext == null) {
            final Thread thread = Thread.currentThread();
            collectingContext = new CollectingContext(thread.getId(), thread.getName());
            THREAD_LOCAL.set(collectingContext);
            WatcherContext.RUNNING_THREADS.put(thread.getId(), thread);
            thread.setUncaughtExceptionHandler(WatcherExceptionHandler.INSTANCE);
        }
        collectingContext.setCurrentPlace(classCanonicalName);
    }

    public String popCurrentPlace() {
        CollectingContext collectingContext = THREAD_LOCAL.get();
        assert collectingContext != null : "collectingContext must be initialized";
        return collectingContext.popCurrentPlace();
    }

    public String getCurrentPlace() {
        return stackWalker
                .walk(ss -> ss.filter(s -> !s.getClassName().startsWith("com.watcher")).findFirst()
                .map(StackWalker.StackFrame::getClassName)
                .orElseThrow(() -> new IllegalStateException("Could not find non watcher caller class")));
    }

    public void onEvent(IterationEnd iterationEnd) {

    }

    public void onEvent(BaseEvent baseEvent) {
        final Thread thread = Thread.currentThread();
        CollectingContext collectingContext = THREAD_LOCAL.get();

        //TODO move this to the native agent (thread start callback)
        if (collectingContext == null) {
            collectingContext = new CollectingContext(thread.getId(), thread.getName());
            collectingContext.addEvent(baseEvent);
            THREAD_LOCAL.set(collectingContext);
            WatcherContext.RUNNING_THREADS.put(thread.getId(), thread);
            thread.setUncaughtExceptionHandler(WatcherExceptionHandler.INSTANCE);
        } else {
            collectingContext.addEvent(baseEvent);
        }

        baseEvent.setLine(collectingContext.getCurrentLine());
    }

    public void setCurrentLine(final int line) {
        final Thread thread = Thread.currentThread();
        CollectingContext collectingContext = THREAD_LOCAL.get();

        if (collectingContext == null) {
            collectingContext = new CollectingContext(thread.getId(), thread.getName(), line);
            THREAD_LOCAL.set(collectingContext);
            WatcherContext.RUNNING_THREADS.put(thread.getId(), thread);
            thread.setUncaughtExceptionHandler(WatcherExceptionHandler.INSTANCE);
        } else {
            collectingContext.setCurrentLine(line);
        }
    }

    public int popCurrentLine() {
        CollectingContext collectingContext = THREAD_LOCAL.get();
        assert collectingContext != null : "collectingContext must be initialized";
        return collectingContext.popCurrentLine();
    }

    public void breakpointReached(final String classCanonicalName, final int line) {
        final Thread thread = Thread.currentThread();
        final String threadName = thread.getName();
        final CollectingContext collectingContext = THREAD_LOCAL.get();

        if (collectingContext != null) {
            var now = Instant.now();
            final List<BaseEvent> events = collectingContext.chopEvents();
            collectingContext.addEvent(new BreakpointReachedMarker(classCanonicalName, line));
            final BreakpointData breakpointData = new BreakpointData(
                    threadName,
                    classCanonicalName,
                    line,
                    events,
                    UUID.randomUUID().toString(),
                    now.getEpochSecond(),
                    now.getNano()
            );
            WatcherContext.getInstance().publishBreakpointData(breakpointData);
        }
    }

    public void iterationEnd() {
        CollectingContext collectingContext = THREAD_LOCAL.get();
        assert collectingContext != null : "collectingContext must be initialized";

        collectingContext.chopEvents(EventType.breakpointReached);

        List<BaseEvent> events = collectingContext.chopEvents(EventType.LoopStart);
        BaseEvent baseEvent = events.get(0);
        LoopStart loopStart = (LoopStart) baseEvent;

        List<BaseEvent> baseEvents = new ArrayList<>(events.subList(1, events.size()));
        List<List<BaseEvent>> list = new ArrayList<>();
        list.add(baseEvents);
        loopStart.setIterations(list);

        collectingContext.addEvent(loopStart);
    }

    public void loopFinish() {
        iterationEnd();
    }
}
