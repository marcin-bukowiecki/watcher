package com.watcher.events;

/**
 * @author Marcin Bukowiecki
 */
public enum EventType {

    Unknown,

    GetLocal,

    GetStatic,

    GetInstance,

    InvokeMethod,

    MethodReturn,

    ExceptionThrown,

    ExceptionThrow,

    MethodEnter,

    MethodExit,

    LoopStart,

    IterationEnd,

    breakpointReached,
}
