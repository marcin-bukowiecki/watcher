package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Marcin Bukowiecki
 */
@Getter
@Setter
@ToString
public class IterationEnd extends BaseEvent {

    public IterationEnd(String place) {
        super(EventType.IterationEnd, place);
    }
}
