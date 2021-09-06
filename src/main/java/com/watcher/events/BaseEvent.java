package com.watcher.events;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Marcin Bukowiecki
 */
@Getter
@Setter
public abstract class BaseEvent {

    protected int line = -1;

    EventType eventType;

    String place;

    public BaseEvent(final EventType eventType, final String place) {
        this.eventType = eventType;
        this.place = place;
    }
}
