package com.watcher.events;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Marcin Bukowiecki
 */
@Getter
@Setter
public class ExceptionThrowEvent extends BaseEvent {

    private String descriptor;

    public ExceptionThrowEvent(String descriptor, String place) {
        super(EventType.ExceptionThrow, place);
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "ExceptionThrowEvent(" +
                "descriptor='" + descriptor + '\'' +
                "line='" + getLine() + '\'' +
                "place='" + getPlace() + '\'' +
                ')';
    }
}
