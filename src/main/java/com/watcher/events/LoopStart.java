package com.watcher.events;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
@Getter
@Setter
@ToString
public class LoopStart extends BaseEvent {

    private List<List<BaseEvent>> iterations = Lists.newArrayList();

    public LoopStart(String place) {
        super(EventType.LoopStart, place);
    }
}
