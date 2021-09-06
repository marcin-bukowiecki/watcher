package com.watcher.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * @author Marcin Bukowiecki
 */
@AllArgsConstructor
@Getter
public class EntryPoint {

    private final String classCanonicalName;

    private final String methodName;

    private final int lineNumber;

    private final String exceptionMessage;

    private final String exceptionCanonicalName;

    private final String threadName;

    private final long threadId;
}
