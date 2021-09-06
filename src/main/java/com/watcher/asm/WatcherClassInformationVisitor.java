package com.watcher.asm;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.mutable.MutableInt;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherClassInformationVisitor extends ClassVisitor {

    private final Map<String, Map<Integer, String>> mappedLocals = new HashMap<>();

    private final IdentityHashMap<Label, Integer> visitedLabels = Maps.newIdentityHashMap();

    private final Map<String, Set<Integer>> loopLabels = new HashMap<>();

    private final Map<String, Set<Integer>> loopEndLabels = new HashMap<>();

    public WatcherClassInformationVisitor(final int api) {
        super(api);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final Map<Integer, String> locals = new HashMap<>();
        final String id = name + descriptor;

        loopLabels.put(id, Sets.newHashSet());
        loopEndLabels.put(id, Sets.newHashSet());

        mappedLocals.put(id, locals);
        MutableInt labelCounter = new MutableInt(0);

        return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

            @Override
            public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                super.visitLocalVariable(name, descriptor, signature, start, end, index);
                locals.put(index, name);
            }

            @Override
            public void visitLabel(Label label) {
                super.visitLabel(label);
                visitedLabels.put(label, labelCounter.getAndIncrement());
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
                super.visitJumpInsn(opcode, label);
                if (Opcodes.GOTO == opcode) {
                    if (visitedLabels.containsKey(label)) {
                        Integer integer = visitedLabels.get(label);
                        loopLabels.get(id).add(integer);
                    }
                }
            }
        };
    }

    public Set<Integer> getLoopLabels(String nameAndDescriptor) {
        return Collections.unmodifiableSet(loopLabels.get(nameAndDescriptor));
    }

    public Map<String, Map<Integer, String>> getMappedLocals() {
        return Collections.unmodifiableMap(mappedLocals);
    }
}
