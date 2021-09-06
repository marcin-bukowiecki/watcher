package com.watcher.logger;

import com.watcher.Feature;
import com.watcher.WatcherContext;
import com.watcher.asm.WatcherClassWriter;
import com.watcher.asm.WatcherLoggerTransformer;
import com.watcher.model.LoggerLine;
import com.watcher.model.TransformationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.util.Optional;
import java.util.Set;

/**
 * @author Marcin Bukowiecki
 */
public class LoggerFeature implements Feature {

    private final Set<LoggerLine> loggerLines;

    public LoggerFeature(final Set<LoggerLine> loggerLines) {
        this.loggerLines = loggerLines;
    }

    @Override
    public void handleTransformation(final TransformationContext transformationContext) {
        var cr = new ClassReader(transformationContext.getBytes());
        var cw = new WatcherClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        var watcherLoggerTransformer = new WatcherLoggerTransformer(WatcherContext.API, cw, this, transformationContext);
        cr.accept(watcherLoggerTransformer, 0);
        transformationContext.setBytes(cw.toByteArray());
    }

    @Override
    public int getPriority() {
        return 10;
    }

    public Optional<LoggerLine> findLoggerLine(final String classCanonicalName, final int line) {
        for (LoggerLine loggerLine : loggerLines) {
            if (loggerLine.getClassCanonicalName().equals(classCanonicalName) && loggerLine.getLine() == line) {
                return Optional.of(loggerLine);
            }
        }
        return Optional.empty();
    }
}
