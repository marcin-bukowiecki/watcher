package com.watcher.asm;

import com.watcher.model.TransformationContext;
import com.watcher.utils.Node;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherDataCollectorClassVisitor extends ClassVisitor {

    private final WatcherClassInformationVisitor watcherClassInformationVisitor;

    private final TransformationContext transformationContext;

    public WatcherDataCollectorClassVisitor(final int api,
                                            final ClassVisitor classVisitor,
                                            final WatcherClassInformationVisitor watcherClassInformationVisitor,
                                            final TransformationContext transformationContext) {

        super(api, classVisitor);
        this.watcherClassInformationVisitor = watcherClassInformationVisitor;
        this.transformationContext = transformationContext;
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        super.visitAttribute(attribute);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("<clinit>") || Modifier.isAbstract(access) || Modifier.isNative(access)) {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

        Node node;

        if (name.equals("<init>")) {
            node = Node.CONSTRUCTOR;
        } else if (Modifier.isStatic(access)) {
            node = Node.STATIC_METHOD;
        } else {
            node = Node.INSTANCE_METHOD;
        }

        return new WatcherDataCollectorVisitor(this, api, node, name, desc, super.visitMethod(access, name, desc, signature, exceptions));
    }

    public TransformationContext getTransformationContext() {
        return transformationContext;
    }

    public WatcherClassInformationVisitor getWatcherClassInformationVisitor() {
        return watcherClassInformationVisitor;
    }
}
