package com.watcher.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.watcher.events.serializer.WatcherArgsSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Marcin Bukowiecki
 */
@Getter
@Setter
@ToString
public class MethodEnterEvent extends BaseEvent {

    private String descriptor;

    private String name;

    @JsonSerialize(using = WatcherArgsSerializer.class)
    private Object[] args;

    public MethodEnterEvent(String name, String descriptor, String place, Object[] args) {
        super(EventType.MethodEnter, place);
        this.descriptor = descriptor;
        this.name = name;
        this.args = args;
    }
}
