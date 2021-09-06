package com.watcher.asm;

import com.watcher.StackValueHandler;
import com.watcher.WatcherContext;
import com.watcher.model.Breakpoint;
import com.watcher.utils.BreakpointMatcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 *
 * Test validator for class transformation
 */
public class WatcherAsmClassValidator extends ClassVisitor {

    private final String classCanonicalName;

    private final List<BreakpointMatcher> expectedBreakpoints;

    public WatcherAsmClassValidator(String classCanonicalName, List<Breakpoint> expectedBreakpoints) {
        super(WatcherContext.API);
        this.classCanonicalName = classCanonicalName;
        this.expectedBreakpoints = expectedBreakpoints.stream().map(BreakpointMatcher::new).collect(Collectors.toList());
    }

    public List<BreakpointMatcher> getExpectedBreakpoints() {
        return expectedBreakpoints;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
            int currentLine = -1;

            @Override
            public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line, start);
                this.currentLine = line;
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                if (name.equals("reachBreakpoint") && owner.equals(Type.getInternalName(StackValueHandler.class))) {
                    expectedBreakpoints.stream()
                            .filter(b -> b.getBreakpoint().equals(Breakpoint.builder()
                                    .classCanonicalName(classCanonicalName)
                                    .line(currentLine).build()))
                            .findFirst()
                            .ifPresent(b -> b.setMatched(true));
                }
            }
        };
    }
}
