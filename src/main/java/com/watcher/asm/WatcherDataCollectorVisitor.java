package com.watcher.asm;

import com.google.common.collect.Maps;
import com.watcher.utils.Node;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherDataCollectorVisitor extends MethodVisitor {

    public static final boolean methodEnterDisabled = true;

    private int lineNumber = -1;
    private final Node node;
    private final String nameAndDescriptor;
    private int stackSize = 0;
    private int currentMaxSize = 0;
    private final WatcherDataCollectorClassVisitor watcherDataCollectorClassVisitor;
    private final String name;
    private final String descriptor;
    private int labelCounter = 0;
    private final Map<Label, Integer> visitedLabels = Maps.newIdentityHashMap();
    private Consumer<Label> afterGoToCallback = label -> {};

    WatcherDataCollectorVisitor(WatcherDataCollectorClassVisitor watcherDataCollectorClassVisitor,
                                int api,
                                Node node,
                                String name,
                                String descriptor,
                                MethodVisitor mv) {
        super(api, mv);
        this.node = node;
        this.nameAndDescriptor = name + descriptor;
        this.watcherDataCollectorClassVisitor = watcherDataCollectorClassVisitor;
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
        Type fieldType = Type.getType(descriptor);

        if (opcode == Opcodes.PUTFIELD) {
            if (fieldType == Type.DOUBLE_TYPE || fieldType == Type.LONG_TYPE) {
                changeMaxSize(-3);
            } else {
                changeMaxSize(-2);
            }
        } else if (opcode == Opcodes.PUTSTATIC) {
            if (fieldType == Type.DOUBLE_TYPE || fieldType == Type.LONG_TYPE) {
                changeMaxSize(-2);
            } else {
                changeMaxSize(-1);
            }
        } else if (opcode == Opcodes.GETFIELD) {
            if (fieldType == Type.DOUBLE_TYPE || fieldType == Type.LONG_TYPE) {
                changeMaxSize(1);
            }
            visitLdcInsn(owner);
            visitLdcInsn(name);
            String signature = getFieldSignature(Type.getType(descriptor));
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getInstanceField", signature, false);
            if (fieldType.getDescriptor().startsWith("L")) {
                visitTypeInsn(Opcodes.CHECKCAST, fieldType.getInternalName());
            }
        } else if (opcode == Opcodes.GETSTATIC) {
            if (fieldType == Type.DOUBLE_TYPE || fieldType == Type.LONG_TYPE) {
                changeMaxSize(2);
            } else {
                changeMaxSize(1);
            }
            visitLdcInsn(owner);
            visitLdcInsn(name);
            String signature = getFieldSignature(Type.getType(descriptor));
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getStaticField", signature, false);
            if (fieldType.getDescriptor().startsWith("L")) {
                visitTypeInsn(Opcodes.CHECKCAST, fieldType.getInternalName());
            }
        }
    }

    public void visitVarInsn(final int opcode, final int var) {
        super.visitVarInsn(opcode, var);

        Map<String, Map<Integer, String>> mappedLocals = watcherDataCollectorClassVisitor.getWatcherClassInformationVisitor().getMappedLocals();
        Map<Integer, String> locals = mappedLocals.get(nameAndDescriptor);
        if (MapUtils.isEmpty(locals)) {
            return;
        }
        String name = locals.get(var);
        if (StringUtils.isEmpty(name)) {
            return;
        }

        if (opcode == Opcodes.LLOAD) {
            visitLdcInsn(var);
            visitLdcInsn(name);
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getLocal", "(JILjava/lang/String;)J", false);
        } else if (opcode == Opcodes.ILOAD) {
            visitLdcInsn(var);
            visitLdcInsn(name);
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getLocal", "(IILjava/lang/String;)I", false);
        } else if (opcode == Opcodes.FLOAD) {
            visitLdcInsn(var);
            visitLdcInsn(name);
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getLocal", "(FILjava/lang/String;)F", false);
        } else if (opcode == Opcodes.DLOAD) {
            visitLdcInsn(var);
            visitLdcInsn(name);
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getLocal", "(DILjava/lang/String;)D", false);
        } else if (opcode == Opcodes.ALOAD) {
            if (var != 0 || node != Node.CONSTRUCTOR) {
                visitInsn(Opcodes.DUP);
                visitLdcInsn(var);
                visitLdcInsn(name);
                visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "getLocal", "(Ljava/lang/Object;ILjava/lang/String;)V", false);
            }
        }

        if (var == 0 && node == Node.CONSTRUCTOR) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.ISTORE || opcode == Opcodes.ASTORE || opcode == Opcodes.FSTORE) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.DLOAD || opcode == Opcodes.LLOAD) {
            changeMaxSize(2);
        } else if (opcode == Opcodes.ILOAD || opcode == Opcodes.FLOAD || opcode == Opcodes.ALOAD) {
            changeMaxSize(1);
        } else {
            throw new UnsupportedOperationException(String.valueOf(opcode));
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        //if (!name.equals("<init>")) {
            visitLdcInsn(owner);
            visitLdcInsn(name);
            visitLdcInsn(descriptor);
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "methodInvoke", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false);
        //}

        visitMethodInsn0(opcode, owner, name, descriptor, isInterface);

        final Type returnType = Type.getReturnType(descriptor);
        if (returnType == Type.VOID_TYPE) {
            //visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "methodReturn", "()V", false);
        } else {
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "methodReturn", getMethodSignature(returnType), false);
            if (returnType.getDescriptor().startsWith("L")) {
                visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
            }
        }
    }

    private void visitMethodInsn0(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

        int toDecrement = 0;

        if (opcode != Opcodes.INVOKESTATIC) {
            toDecrement-=1;
        }

        for (Type argumentType : Type.getArgumentTypes(descriptor)) {
            if (argumentType == Type.DOUBLE_TYPE || argumentType == Type.LONG_TYPE) {
                toDecrement-=2;
            } else {
                toDecrement-=1;
            }
        }

        final Type returnType = Type.getReturnType(descriptor);
        if (returnType == Type.VOID_TYPE) {

        } else if (returnType == Type.DOUBLE_TYPE || returnType == Type.LONG_TYPE) {
            toDecrement+=2;
        } else {
            toDecrement+=1;
        }

        changeMaxSize(toDecrement);
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(value);
        if (value instanceof Long || value instanceof Double) {
            changeMaxSize(2);
        } else {
            changeMaxSize(1);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.ATHROW) {
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "exceptionThrown", Type.getMethodDescriptor(Type.getType(Throwable.class), Type.getType(Throwable.class)), false);
        }

        if (opcode == Opcodes.RETURN || opcode == Opcodes.ARETURN || opcode == Opcodes.FRETURN || opcode == Opcodes.IRETURN || opcode == Opcodes.DRETURN || opcode == Opcodes.LRETURN) {
            //super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "methodExit", "()V", false);
            super.visitInsn(opcode);
            return;
        }

        super.visitInsn(opcode);


        if (opcode == Opcodes.ARRAYLENGTH) {
            return;
        }

        if (opcode == Opcodes.AALOAD || opcode == Opcodes.CALOAD
                || opcode == Opcodes.LSHL
                || opcode == Opcodes.ISHL) {
            changeMaxSize(-1);
            return;
        }

        if (opcode == Opcodes.LALOAD || opcode == Opcodes.INEG || opcode == Opcodes.LNEG
                || opcode == Opcodes.I2S
                || opcode == Opcodes.I2C
                || opcode == Opcodes.I2B) {
            return;
        }

        if (opcode == Opcodes.LCMP) {
            changeMaxSize(-3);
            return;
        }

        if (opcode == Opcodes.ATHROW || opcode == Opcodes.POP) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.ICONST_0 || opcode == Opcodes.ICONST_1 || opcode == Opcodes.ICONST_2 || opcode == Opcodes.ICONST_3 ||
                opcode == Opcodes.ICONST_4 || opcode == Opcodes.ICONST_5 || opcode == Opcodes.ICONST_M1) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.IADD || opcode == Opcodes.FADD) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.DADD || opcode == Opcodes.LADD) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.DUP) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.BALOAD) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.ISUB || opcode == Opcodes.FSUB) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.DSUB || opcode == Opcodes.LSUB) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1) {
            changeMaxSize(2);
        } else if (opcode == Opcodes.LDIV || opcode == Opcodes.DDIV || opcode == Opcodes.LMUL || opcode == Opcodes.DMUL) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.I2L || opcode == Opcodes.I2D) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.D2I || opcode == Opcodes.D2F) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.ACONST_NULL) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.AASTORE || opcode == Opcodes.IASTORE) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IAND) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.L2I) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IALOAD) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.LREM) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.IREM) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.DUP_X1) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.IMUL) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.LUSHR) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IDIV) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IOR) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.LOR || opcode == Opcodes.LXOR || opcode == Opcodes.LAND || opcode == Opcodes.IAND) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.IXOR) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IUSHR) {
            changeMaxSize(-1);
        } else {
            throw new UnsupportedOperationException(String.valueOf(opcode));
        }
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (opcode == Opcodes.GOTO) {
            if (visitedLabels.containsKey(label)) {
                visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "iterationEnd",
                        Type.getMethodDescriptor(Type.getType(void.class)), false);
                this.afterGoToCallback = (Label l) -> {
                    visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "loopFinish",
                            Type.getMethodDescriptor(Type.getType(void.class)), false);
                };
            }
        }

        super.visitJumpInsn(opcode, label);

        if (opcode == Opcodes.GOTO) {
            return;
        }
        if (opcode == Opcodes.IFNULL) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IFLE) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IF_ICMPLE || opcode == Opcodes.IF_ICMPLT || opcode == Opcodes.IF_ICMPNE) {
            changeMaxSize(-2);
        } else if (opcode == Opcodes.IFEQ || opcode == Opcodes.IFGE) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IFNE) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IFLT) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IFNONNULL) {
            changeMaxSize(-1);
        } else if (opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IF_ACMPEQ || opcode == Opcodes.IF_ACMPNE) {
            changeMaxSize(-2);
        } else {
            throw new UnsupportedOperationException(String.valueOf(opcode));
        }
    }

    @Override
    public void visitLabel(Label label) {
        if (watcherDataCollectorClassVisitor.getWatcherClassInformationVisitor().getLoopLabels(this.nameAndDescriptor).contains(labelCounter)) {
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "loopStart",
                    Type.getMethodDescriptor(Type.getType(void.class)), false);
            visitedLabels.put(label, labelCounter);
        }
        super.visitLabel(label);
        if (this.afterGoToCallback != null) {
            this.afterGoToCallback.accept(label);
            this.afterGoToCallback = null;
        }
        labelCounter++;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(currentMaxSize, maxLocals);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand);

        if (opcode == Opcodes.BIPUSH) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.SIPUSH) {
            changeMaxSize(1);
        } else if (opcode == Opcodes.NEWARRAY) {
            changeMaxSize(1);
        } else {
            throw new UnsupportedOperationException(String.valueOf(opcode));
        }
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        super.visitFrame(type, numLocal, local, numStack, stack);
        /*
        if (type == Opcodes.F_SAME || type == Opcodes.F_SAME1) {
            super.visitFrame(type, numLocal, local, numStack, Arrays.copyOf(stack, currentMaxSize));
        } else if (type == Opcodes.F_APPEND) {
            super.visitFrame(type, numLocal, local, numStack, Arrays.copyOf(stack, currentMaxSize));
        } else if (type == Opcodes.F_CHOP) {
            super.visitFrame(type, numLocal, local, numStack, Arrays.copyOf(stack, currentMaxSize));
        } else if (type == Opcodes.F_FULL) {
            //super.visitFrame(type, numLocal, local, numStack, Arrays.copyOf(stack, currentMaxSize));
        } else {
            throw new UnsupportedOperationException(String.valueOf(type));
        }*/
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        int wasLineNumber = this.lineNumber;
        this.lineNumber = line;
        super.visitLineNumber(line, start);
        visitLdcInsn(line);
        visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "currentLine", "(I)V", false);

        if (!methodEnterDisabled && wasLineNumber == -1) {
            visitLdcInsn(name);
            visitLdcInsn(descriptor);
            visitLdcInsn(watcherDataCollectorClassVisitor.getTransformationContext().getClassCanonicalName());
            Type[] argumentTypes = Type.getArgumentTypes(descriptor);
            int noOfArguments = argumentTypes.length;
            super.visitLdcInsn(noOfArguments);
            super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            int i = 0;
            boolean isStatic = node == Node.STATIC_METHOD;
            for (Type argumentType : argumentTypes) {
                super.visitInsn(Opcodes.DUP);
                super.visitLdcInsn(i);
                int localIndex = isStatic ? i : i + 1;
                if (argumentType == Type.INT_TYPE) {
                    super.visitVarInsn(Opcodes.ILOAD, localIndex);
                } else if (argumentType == Type.LONG_TYPE) {
                    super.visitVarInsn(Opcodes.LLOAD, localIndex);
                } else if (argumentType == Type.DOUBLE_TYPE) {
                    super.visitVarInsn(Opcodes.DLOAD, localIndex);
                } else if (argumentType == Type.FLOAT_TYPE) {
                    super.visitVarInsn(Opcodes.FLOAD, localIndex);
                } else {
                    super.visitVarInsn(Opcodes.ALOAD, localIndex);
                }
                if (argumentType.getDescriptor().startsWith("L")) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Object.class)), false);
                } else if (argumentType.getDescriptor().startsWith("[")) {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(Object.class)), false);
                } else {
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", Type.getMethodDescriptor(Type.getType(String.class), argumentType), false);
                }
                super.visitInsn(Opcodes.AASTORE);
                i++;
            }
            visitMethodInsn0(Opcodes.INVOKESTATIC, "com/watcher/StackValueHandler", "methodEnter", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(String.class), Type.getType(String.class), Type.getType(String.class), Type.getType(Object[].class)), false);
        }
    }

    private void changeMaxSize(int by) {
        stackSize = stackSize + by;
        if (stackSize < 0) {
            stackSize = 0;
        } else if (stackSize > currentMaxSize) {
            currentMaxSize = stackSize;
        }
    }

    private static String getFieldSignature(final Type type) {
        String signature;

        if (type.equals(Type.INT_TYPE)) {
            signature = "(ILjava/lang/String;Ljava/lang/String;)I";
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            signature = "(ZLjava/lang/String;Ljava/lang/String;)Z";
        } else if (type.equals(Type.CHAR_TYPE)) {
            signature = "(CLjava/lang/String;Ljava/lang/String;)C";
        } else if (type.equals(Type.SHORT_TYPE)) {
            signature = "(SLjava/lang/String;Ljava/lang/String;)S";
        } else if (type.equals(Type.BYTE_TYPE)) {
            signature = "(BLjava/lang/String;Ljava/lang/String;)B";
        } else if (type.equals(Type.LONG_TYPE)) {
            signature = "(JLjava/lang/String;Ljava/lang/String;)J";
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            signature = "(DLjava/lang/String;Ljava/lang/String;)D";
        } else if (type.equals(Type.FLOAT_TYPE)) {
            signature = "(FLjava/lang/String;Ljava/lang/String;)F";
        } else {
            signature = "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;";
        }

        return signature;
    }

    private static String getMethodSignature(final Type type) {
        String signature;

        if (type.equals(Type.INT_TYPE)) {
            signature = "(I)I";
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            signature = "(Z)Z";
        } else if (type.equals(Type.CHAR_TYPE)) {
            signature = "(C)C";
        } else if (type.equals(Type.SHORT_TYPE)) {
            signature = "(S)S";
        } else if (type.equals(Type.BYTE_TYPE)) {
            signature = "(B)B";
        } else if (type.equals(Type.LONG_TYPE)) {
            signature = "(J)J";
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            signature = "(D)D";
        } else if (type.equals(Type.FLOAT_TYPE)) {
            signature = "(F)F";
        } else {
            signature = "(Ljava/lang/Object;)Ljava/lang/Object;";
        }

        return signature;
    }
}
