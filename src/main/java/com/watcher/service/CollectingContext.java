package com.watcher.service;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.watcher.events.BaseEvent;
import com.watcher.events.EventType;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 */
public final class CollectingContext {

    private final long threadId;

    private final String name;

    private final LinkedList<Integer> currentLine = Lists.newLinkedList();

    private final LinkedList<String> currentPlace = Lists.newLinkedList();

    private EvictingQueue<BaseEvent> events = EvictingQueue.create(Integer.parseInt(System.getProperty("watcher.collectorBufferSize", "100")));

    public CollectingContext(final long threadId, final String name) {
        this.threadId = threadId;
        this.name = name;
        ThreadLocalCollector.collectingRegister.add(this);
    }

    public CollectingContext(final long threadId, final String name, final int currentLine) {
        this.threadId = threadId;
        this.name = name;
        setCurrentLine(currentLine);
        ThreadLocalCollector.collectingRegister.add(this);
    }

    public void setCurrentPlace(String currentPlace) {
        this.currentPlace.addLast(currentPlace);
    }

    public String getCurrentPlace() {
        return currentPlace.peekLast();
    }

    public String popCurrentPlace() {
        if (this.currentPlace.isEmpty()) {
            return null;
        }
        return this.currentPlace.removeLast();
    }

    public final void addEvent(BaseEvent baseEvent) {
        events.add(baseEvent);
    }

    public final void setCurrentLine(int currentLine) {
        this.currentLine.addLast(currentLine);
    }

    public final int getCurrentLine() {
        if (this.currentLine.isEmpty()) {
            return -1;
        }
        return currentLine.getLast();
    }

    public int popCurrentLine() {
        if (this.currentLine.isEmpty()) {
            return -1;
        }
        return this.currentLine.removeLast();
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(name)) {
            return threadId + "-NO_NAME";
        } else {
            return threadId + "-" + name;
        }
    }

    public final List<BaseEvent> chopEvents(EventType until) {
        List<BaseEvent> result = events.stream().dropWhile(p -> p.getEventType() != until).collect(Collectors.toList());

        EvictingQueue<BaseEvent> newBuffer = EvictingQueue.create(Integer.parseInt(System.getProperty("watcher.collectorBufferSize", "100")));
        for (BaseEvent event : events) {
            if (event == result.get(0)) {
                break;
            } else {
                newBuffer.add(event);
            }
        }
        this.events = newBuffer;

        return result;
    }

    public final List<BaseEvent> chopEvents() {
        final BaseEvent[] baseEvents = (BaseEvent[]) events.toArray(new BaseEvent[0]);
        events.clear();
        return Arrays.asList(baseEvents);
    }

    public BaseEvent lastEvent() {
        return null;
    }
}
