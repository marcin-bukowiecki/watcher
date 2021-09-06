package com.watcher.handlers;

import com.watcher.WatcherContext;
import com.watcher.model.WatcherSession;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AbstractDebugHandler {

    public void transform(Instrumentation instrumentation, WatcherSession watcherSession) {
        try {
            Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();

            List<Class<?>> classesToTransform = Arrays.stream(allLoadedClasses)
                    .filter(aClass -> {
                        try {
                            return WatcherContext.getInstance().getWatcherSession()
                                    .getTransformContext()
                                    .isToTransform(aClass.getCanonicalName());
                        } catch (NoClassDefFoundError ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    })
                    .filter(instrumentation::isModifiableClass)
                    .collect(Collectors.toList());

            if (WatcherContext.logEnabled()) {
                log.info("Got {} classes to instrument", classesToTransform.size());
            }

            for (Class<?> aClass : classesToTransform) {
                instrumentation.retransformClasses(aClass);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }

        if (WatcherContext.logEnabled()) {
            log.info("Finished instrumenting");
        }
    }
}
