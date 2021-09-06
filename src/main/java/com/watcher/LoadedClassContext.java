package com.watcher;

import com.watcher.model.Breakpoint;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public final class LoadedClassContext {

    private ReentrantLock lock = new ReentrantLock();

    private int classId;

    private String canonicalClassName;

    private byte[] actualBytecode;

    private Set<Breakpoint> breakpoints = new HashSet<>();

    private volatile AtomicInteger transformationCounter = new AtomicInteger(0);

    public LoadedClassContext(int classId, String canonicalClassName) {
        this.classId = classId;
        this.canonicalClassName = canonicalClassName;
    }

    public int version() {
        return transformationCounter.get();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void setActualBytecode(byte[] actualBytecode) {
        this.actualBytecode = actualBytecode;
    }

    public byte[] getActualBytecode() {
        return actualBytecode;
    }

    public boolean addBreakpoint(Breakpoint breakpoint) {
        if (!lock.isHeldByCurrentThread()) {
            throw new UnsupportedOperationException("Current thread " + Thread.currentThread().getName() + " isn't holding lock to add breakpoint");
        }
        return !breakpoints.add(breakpoint);
    }

    public String getCanonicalClassName() {
        return canonicalClassName;
    }

    public int getClassId() {
        return classId;
    }

    public int incrementCounter() {
        return transformationCounter.incrementAndGet();
    }

    public Set<Breakpoint> copyBreakpoints() {
        lock.lock();
        Set<Breakpoint> breakpoints = Set.copyOf(this.breakpoints);
        lock.unlock();
        return breakpoints;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean removeBreakpoint(Breakpoint breakpoint) {
        if (!lock.isHeldByCurrentThread()) {
            throw new UnsupportedOperationException();
        } else {
            return this.breakpoints.remove(breakpoint);
        }
    }

    public static final class Builder {

        private int classId;

        private String canonicalClassName;

        public Builder classId(int classId) {
            this.classId = classId;
            return this;
        }

        public Builder canonicalClassName(String canonicalClassName) {
            this.canonicalClassName = canonicalClassName;
            return this;
        }

        public LoadedClassContext build() {
            return new LoadedClassContext(classId, canonicalClassName);
        }
    }
}
