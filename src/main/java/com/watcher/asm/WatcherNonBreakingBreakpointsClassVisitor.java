package com.watcher.asm;

import com.watcher.StackValueHandler;
import com.watcher.WatcherContext;
import com.watcher.model.Breakpoint;
import com.watcher.model.TransformationContext;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class WatcherNonBreakingBreakpointsClassVisitor extends ClassVisitor {

    private final Map<Integer, Breakpoint> mappedBreakpoints;

    private int addedBreakpoints = 0;

    private final TransformationContext transformationContext;

    public WatcherNonBreakingBreakpointsClassVisitor(int api, ClassVisitor classVisitor,
                                                     TransformationContext transformationContext) {
        super(api, classVisitor);
        this.mappedBreakpoints = transformationContext.getBreakpoints()
                .stream()
                .collect(Collectors.toMap(Breakpoint::getLine, Function.identity()));
        this.transformationContext = transformationContext;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        return new MethodVisitor(this.api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

            private Runnable breakpointToAdd = null;

            private int lastLineNumber = -1;

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                if (StackValueHandler.getDescriptor().equals(owner) && "currentLine".equals(name)) {
                    Breakpoint breakpoint = mappedBreakpoints.get(lastLineNumber);
                    if (breakpoint != null) {
                        log.info("Adding breakpoint at {} line: {}", breakpoint.getClassCanonicalName(),
                                breakpoint.getLine());
                        addedBreakpoints++;
                        super.visitLdcInsn(breakpoint.getClassCanonicalName());
                        super.visitLdcInsn(breakpoint.getLine());
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(StackValueHandler.class),
                                "reachBreakpoint", "(Ljava/lang/String;I)V", false);
                    }
                }
            }

            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
                this.lastLineNumber = line;
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.RETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.DRETURN ||
                        opcode == Opcodes.FRETURN || opcode == Opcodes.LRETURN) {
                    if (breakpointToAdd != null) {
                        breakpointToAdd.run();
                        breakpointToAdd = null;
                    }
                }
                super.visitInsn(opcode);
            }
        };
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        log.info("Added {} breakpoints in {}", addedBreakpoints, transformationContext.getClassCanonicalName());
    }
}
