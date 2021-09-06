package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
public class GetStaticField extends BaseEvent {

    private String value;

    private String owner;

    private String name;

    private String descriptor;

    public GetStaticField(String value, String owner, String name, String descriptor, String place) {
        super(EventType.GetStatic, place);
        this.value = value;
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public String toString() {
        return "GetStaticField{" +
                "value='" + value + '\'' +
                ", owner='" + owner + '\'' +
                ", name='" + name + '\'' +
                ", descriptor='" + descriptor + '\'' +
                ", line='" + getLine() + '\'' +
                ", place='" + getPlace() + '\'' +
                '}';
    }
}
