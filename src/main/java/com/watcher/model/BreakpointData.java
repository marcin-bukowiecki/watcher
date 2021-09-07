package com.watcher.model;

import com.watcher.events.BaseEvent;
import lombok.*;

import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class BreakpointData {

    private String threadName;

    private String classCanonicalName;

    private int line;

    private List<BaseEvent> data;

    private String uuid;

    private long epochSecond;

    private int nano;
}
