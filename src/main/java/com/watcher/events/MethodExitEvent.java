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
public class MethodExitEvent extends BaseEvent {

    private String descriptor;

    private String name;

    private Object value;

    public MethodExitEvent(String name, String descriptor, String place, Object value) {
        super(EventType.MethodExit, place);
        this.descriptor = descriptor;
        this.name = name;
        this.value = value;
    }
}
