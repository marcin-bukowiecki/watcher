package com.watcher.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherPrintStackTraceTransformer extends ClassVisitor {

    public WatcherPrintStackTraceTransformer(int api,
                                             ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String descriptor,
                                     String signature,
                                     String[] exceptions) {

        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.RETURN) {
                    if ("printStackTrace".equals(name) && descriptor.equals("()V")) {
                        super.visitVarInsn(Opcodes.ALOAD, 0);
                        super.visitMethodInsn(
                                Opcodes.INVOKESTATIC,
                                "com/watcher/StackValueHandler",
                                "handlePrintStackTrace",
                                "(Ljava/lang/Throwable;)V",
                                false
                        );
                    }
                }
                super.visitInsn(opcode);
            }
        };
    }
}
