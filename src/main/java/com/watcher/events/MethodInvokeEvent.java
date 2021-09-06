package com.watcher.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodInvokeEvent extends BaseEvent {

    private String owner;

    private String name;

    private String descriptor;

    public MethodInvokeEvent(final String owner, final String name, final String descriptor, final String place) {
        super(EventType.InvokeMethod, place);
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "MethodInvokeEvent(" +
                "owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", line='" + getLine() + '\'' +
                ", place='" + getPlace() + '\'' +
                ')';
    }
}
