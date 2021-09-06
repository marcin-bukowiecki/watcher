package com.watcher.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = {"classCanonicalName", "line"})
@Builder
@ToString
public class Breakpoint {

    private String classCanonicalName;

    private int line;

    @Setter
    @Builder.Default
    private BreakpointStatus status = BreakpointStatus.unknown;
}
