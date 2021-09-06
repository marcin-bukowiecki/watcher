package com.watcher;

/**
 * @author Marcin Bukowiecki
 *
 * Enum for Watcher Agent statuses
 */
public enum Status {

    idle,

    addingBreakpointsToLoadedClasses,

    removingBreakpointsFromLoadedClasses,
}
