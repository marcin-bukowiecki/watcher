package com.watcher.model;

import com.watcher.WatcherContext;
import com.watcher.messages.BreakpointStatusMessage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
@Data
public class TransformationContext {

    private byte[] bytes;

    private int classId;

    private String classCanonicalName;

    private Set<Breakpoint> breakpoints;

    private boolean collectData;

    public void afterTransformation(WatcherContext watcherContext) {
        if (CollectionUtils.isNotEmpty(breakpoints)) {
            for (Breakpoint breakpoint : breakpoints) {
                breakpoint.setStatus(BreakpointStatus.active);
            }

            for (Breakpoint breakpoint : breakpoints) {
                BreakpointStatusMessage breakpointStatusMessage = new BreakpointStatusMessage();
                breakpointStatusMessage.setBreakpointStatus(BreakpointStatus.active);
                breakpointStatusMessage.setClassCanonicalName(classCanonicalName);
                breakpointStatusMessage.setLine(breakpoint.getLine());

                log.info("Publishing breakpoint statuses for class {}", classCanonicalName);

                watcherContext.getVertx().eventBus().publish("ws.publish.breakpoint.status", breakpointStatusMessage);
            }
        }
    }
}
