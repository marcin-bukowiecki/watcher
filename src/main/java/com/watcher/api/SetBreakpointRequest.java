package com.watcher.api;

import lombok.Data;

@Data
public class SetBreakpointRequest {

    private String classCanonicalName;

    private int lineNumber;
}
