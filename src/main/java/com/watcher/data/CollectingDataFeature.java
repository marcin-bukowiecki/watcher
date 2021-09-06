package com.watcher.data;

import com.watcher.Feature;
import com.watcher.asm.WatcherClassInformationVisitor;
import com.watcher.asm.WatcherClassWriter;
import com.watcher.asm.WatcherDataCollectorClassVisitor;
import com.watcher.model.TransformationContext;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class CollectingDataFeature implements Feature {

    public CollectingDataFeature() {
    }

    @Override
    public void handleTransformation(final TransformationContext transformationContext) {
        log.info("Adding data collectors for {}", transformationContext.getClassCanonicalName());

        var watcherClassInformationVisitor = new WatcherClassInformationVisitor(Opcodes.ASM7);
        var cr = new ClassReader(transformationContext.getBytes());
        cr.accept(watcherClassInformationVisitor, 0);

        var cw = new WatcherClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        var watcherDataCollectorClassVisitor = new WatcherDataCollectorClassVisitor(Opcodes.ASM7,
                cw,
                watcherClassInformationVisitor,
                transformationContext);
        cr.accept(watcherDataCollectorClassVisitor, 0);

        transformationContext.setBytes(cw.toByteArray());
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public String toString() {
        return "CollectingDataFeature{}";
    }
}
