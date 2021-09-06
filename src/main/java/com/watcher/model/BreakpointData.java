package com.watcher.model;

import com.watcher.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BreakpointData {

    private String threadName;

    private String classCanonicalName;

    private int line;

    private List<BaseEvent> data;

    private String uuid;

    private long epochSecond;

    private int nano;
}
