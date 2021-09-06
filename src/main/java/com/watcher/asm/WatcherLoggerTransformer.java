package com.watcher.asm;

import com.watcher.logger.LoggerFeature;
import com.watcher.model.TransformationContext;
import org.apache.commons.lang3.mutable.MutableInt;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherLoggerTransformer extends ClassVisitor {

    private String classCanonicalName;

    private MutableInt prevLine = new MutableInt(-1);

    private TransformationContext transformationContext;

    private LoggerFeature loggerFeature;

    public WatcherLoggerTransformer(int api, ClassVisitor classVisitor, LoggerFeature loggerFeature, TransformationContext transformationContext) {
        super(api, classVisitor);
        this.transformationContext = transformationContext;
        this.loggerFeature = loggerFeature;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classCanonicalName = name.replace('/', '.');
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

            @Override
            public void visitLineNumber(int line, Label start) {
                loggerFeature.findLoggerLine(classCanonicalName, line).ifPresent(loggerLine -> visitLoggerLine(this));
                prevLine.setValue(line);
                super.visitLineNumber(line, start);
            }
        };
    }

    public void visitLoggerLine(MethodVisitor methodVisitor) {

    }
}
