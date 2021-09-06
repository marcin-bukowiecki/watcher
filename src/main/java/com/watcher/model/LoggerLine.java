package com.watcher.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Marcin Bukowiecki
 */
@Data
@EqualsAndHashCode
public class LoggerLine {

    private String classCanonicalName;

    private String methodName;

    private String methodDescriptor;

    private int line;
}
