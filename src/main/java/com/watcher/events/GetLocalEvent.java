package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetLocalEvent extends BaseEvent {

    private int index;

    private String value;

    private String descriptor;

    private String name;

    public GetLocalEvent(int index, String value, String descriptor, String name, String place) {
        super(EventType.GetLocal, place);
        this.index = index;
        this.value = value;
        this.descriptor = descriptor;
        this.name = name;
    }
}
