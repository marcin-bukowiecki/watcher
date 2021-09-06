package com.watcher.service;

import com.watcher.model.Breakpoint;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
@Builder
@Getter
@ToString
public class TransformationMessage {

    @Builder.Default
    private boolean enableCollectingData = true;

    @Builder.Default
    private List<Breakpoint> breakpointsToAdd = Collections.emptyList();

    @Builder.Default
    private List<Breakpoint> breakpointsToRemove = Collections.emptyList();

}
