package com.watcher.asm;

import org.objectweb.asm.ClassWriter;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherClassWriter extends ClassWriter {

    public WatcherClassWriter(int flags) {
        super(flags);
    }
}
