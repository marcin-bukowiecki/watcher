package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GetInstanceField extends BaseEvent {

    private String owner;

    private String name;

    private String value;

    private String descriptor;

    public GetInstanceField(final String owner, final String name, final String value, final String descriptor, final String place) {
        super(EventType.GetInstance, place);
        this.name = name;
        this.value = value;
        this.descriptor = descriptor;
        this.owner = owner;
    }
}
