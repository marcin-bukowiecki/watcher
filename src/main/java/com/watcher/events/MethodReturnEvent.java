package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MethodReturnEvent extends BaseEvent {

    private String value;

    private String descriptor;

    public MethodReturnEvent(String value, String descriptor, String place) {
        super(EventType.MethodReturn, place);
        this.value = value;
        this.descriptor = descriptor;
    }
}
