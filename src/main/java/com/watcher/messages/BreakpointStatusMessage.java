package com.watcher.messages;

import com.watcher.model.BreakpointStatus;
import lombok.Data;

/**
 * @author Marcin Bukowiecki
 */
@Data
public class BreakpointStatusMessage {

    private String classCanonicalName;

    private int line;

    private BreakpointStatus breakpointStatus;
}
