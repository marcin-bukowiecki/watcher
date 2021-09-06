package com.watcher.events;

public class BreakpointReachedMarker extends BaseEvent {

    public BreakpointReachedMarker(String place, int line) {
        super(EventType.breakpointReached, place);
        this.line = line;
    }
}
