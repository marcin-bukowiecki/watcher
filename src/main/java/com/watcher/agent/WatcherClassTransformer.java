package com.watcher.agent;

import com.watcher.LoadedClassContext;
import com.watcher.WatcherContext;
import com.watcher.asm.WatcherClassWriter;
import com.watcher.asm.WatcherPrintStackTraceTransformer;
import com.watcher.model.TransformationContext;
import com.watcher.service.FeaturesPipeline;
import com.watcher.utils.ByteCodeLogger;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * @author Marcin Bukowiecki
 *
 * Watcher class transformer
 */
@Slf4j
public class WatcherClassTransformer implements ClassFileTransformer {

    private final WatcherContext watcherContext;

    /**
     * Constructor for initializing given transformer
     *
     * @param watcherContext given context
     */
    public WatcherClassTransformer(final WatcherContext watcherContext) {
        this.watcherContext = watcherContext;
    }

    /**
     * Entry point for class transformation
     *
     * If class is not in supported packages (see {@link com.watcher.context.TransformContext}) transformation is ignored
     *
     * @param loader class loader of transformed class
     * @param className loading class name
     * @param classBeingRedefined class object being transformed
     * @param protectionDomain protected domain //TODO learn what is this
     * @param byteCode byte code of given transformed class
     * @return new bytecode fo transformed class
     */
    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] byteCode) {

        if (className == null) {
            return byteCode;
        }

        if (className.equals("java/lang/Throwable")) {
            var cr = new ClassReader(byteCode);
            var cw = new WatcherClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            var watcherDataCollectorClassVisitor = new WatcherPrintStackTraceTransformer(Opcodes.ASM7, cw);
            cr.accept(watcherDataCollectorClassVisitor, 0);
            return cw.toByteArray();
        }

        if (!watcherContext
                .getWatcherSession()
                .getTransformContext()
                .isToTransform(className)) {
            return byteCode;
        }

        var classCanonicalName = className.replace('/', '.');

        if (WatcherContext.logEnabled()) {
            log.info("Got {} to transform", classCanonicalName);
        }

        try {
            return getNewBytes(classCanonicalName, byteCode);
        } catch (Throwable t) {
            log.info("Exception while transforming {}", classCanonicalName, t);
            return byteCode;
        }
    }

    /**
     * Method creates transformation pipeline add changes the bytecode of given class
     *
     * @param classCanonicalName tranformed class
     * @param originalBytes original byte array of transformed class
     * @return result of transformation
     */
    public byte[] getNewBytes(String classCanonicalName, byte[] originalBytes) {
        LoadedClassContext loadedClassContext = watcherContext.getOrCreateLoadedClassContext(classCanonicalName);

        TransformationContext transformationContext = new TransformationContext();
        transformationContext.setBytes(originalBytes);
        transformationContext.setBreakpoints(loadedClassContext.copyBreakpoints());
        transformationContext.setClassCanonicalName(classCanonicalName);
        transformationContext.setClassId(loadedClassContext.getClassId());

        if (loadedClassContext.version() == 0) {
            if (WatcherContext.getInstance().logEnabled) {
                log.info("{} is loaded for first time", classCanonicalName);
            }
        }

        FeaturesPipeline featuresPipeline = watcherContext.featuresPipeline(classCanonicalName);
        boolean transformed = featuresPipeline.handle(transformationContext);

        if (!transformed) {
            return originalBytes;
        }

        byte[] newBytes = transformationContext.getBytes();

        ByteCodeLogger.logASM(newBytes);

        loadedClassContext.setActualBytecode(newBytes);
        loadedClassContext.incrementCounter();

        transformationContext.afterTransformation(watcherContext);

        log.info("Finished transforming {}", classCanonicalName);

        return newBytes;
    }
}
