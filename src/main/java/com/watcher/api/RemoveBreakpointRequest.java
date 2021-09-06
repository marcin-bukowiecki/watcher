package com.watcher.api;

import lombok.Data;

@Data
public class RemoveBreakpointRequest {

    private String classCanonicalName;

    private int lineNumber;
}
