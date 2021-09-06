package com.watcher.utils;

import com.watcher.model.Breakpoint;

/**
 * @author Marcin Bukowiecki
 *
 * Class used as a matcher for unit tests
 */
public class BreakpointMatcher {

    private final Breakpoint breakpoint;

    private boolean matched = false;

    public BreakpointMatcher(Breakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public Breakpoint getBreakpoint() {
        return breakpoint;
    }

    @Override
    public String toString() {
        return "BreakpointMatcher{" +
                "breakpoint=" + breakpoint +
                ", matched=" + matched +
                '}';
    }
}
