package com.watcher.events;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExceptionThrownEvent extends BaseEvent {

    private String exceptionCanonicalName;

    private String classCanonicalName;

    private String name;

    private String exceptionMessage;

    private String threadName;

    private long threadId;

    public ExceptionThrownEvent(String exceptionCanonicalName, String classCanonicalName, String name, String exceptionMessage, String threadName, long threadId, String place) {
        super(EventType.ExceptionThrown, place);
        this.exceptionCanonicalName = exceptionCanonicalName;
        this.classCanonicalName = classCanonicalName;
        this.name = name;
        this.exceptionMessage = exceptionMessage == null ? "" : exceptionMessage;
        this.threadName = threadName;
        this.threadId = threadId;
    }
}
