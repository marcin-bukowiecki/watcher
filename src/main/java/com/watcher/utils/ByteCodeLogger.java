package com.watcher.utils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
public class ByteCodeLogger {

    private static final Printer printer = new Textifier();
    
    private static final TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    public static void logASM(final byte[] bytes) {
        final ClassReader reader = new ClassReader(bytes);
        final ClassNode classNode = new ClassNode();
        reader.accept(classNode,0);

        final List<MethodNode> methods = classNode.methods;
        
        for(MethodNode m: methods){
            InsnList inList = m.instructions;
            System.out.println(m.name);
            for(int i = 0; i< inList.size(); i++){
                System.out.print(instructionToString(inList.get(i)));
            }
        }
    }

    private static String instructionToString(AbstractInsnNode insn){
        insn.accept(mp);
        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();
        return sw.toString();
    }

}
