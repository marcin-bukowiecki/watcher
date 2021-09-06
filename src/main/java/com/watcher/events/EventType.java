package com.watcher.events;

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

    IterationEnd, breakpointReached,
}
