package com.watcher.breakpoint;

import com.watcher.Feature;
import com.watcher.WatcherContext;
import com.watcher.asm.WatcherClassWriter;
import com.watcher.asm.WatcherNonBreakingBreakpointsClassVisitor;
import com.watcher.model.TransformationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class BreakpointFeature implements Feature {

    public BreakpointFeature() {
    }

    @Override
    public void handleTransformation(final TransformationContext transformationContext) {
        var cr = new ClassReader(transformationContext.getBytes());
        var cw = new WatcherClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        var watcherNonBreakingBreakpointsClassVisitor = new WatcherNonBreakingBreakpointsClassVisitor(WatcherContext.API, cw, transformationContext);
        cr.accept(watcherNonBreakingBreakpointsClassVisitor, 0);
        transformationContext.setBytes(cw.toByteArray());
    }

    @Override
    public int getPriority() {
        return 9;
    }

    @Override
    public String toString() {
        return "BreakpointFeature{}";
    }
}
