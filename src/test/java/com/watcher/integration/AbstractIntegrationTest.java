package com.watcher.integration;

import com.watcher.DebugSessionStatus;
import com.watcher.WatcherContext;
import com.watcher.WatcherContextProvider;
import com.watcher.agent.WatcherClassTransformer;
import com.watcher.api.WatcherApi;
import com.watcher.handlers.DebugSessionHandler;
import com.watcher.handlers.SetBreakpointHandler;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.Breakpoint;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.BeforeClass;

import java.lang.instrument.Instrumentation;

/**
 * @author Marcin Bukowiecki
 */
public abstract class AbstractIntegrationTest {

    protected static WatcherContext watcherContext;

    @BeforeClass
    public static void setup() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        watcherContext = new WatcherContext(instrumentation, new WatcherContextProvider());
        WatcherApi.init(watcherContext);
        instrumentation.addTransformer(new WatcherClassTransformer(watcherContext), true);
        watcherContext.printASM = true;
    }

    //syntax sugar for forcing JVM to load class:)
    public static void load(Class<?> clazz) {

    }

    public static <T> T setBreakpoint(Class<?> clazz, int line) {
        try {
            var instance = clazz.getConstructor().newInstance();

            new DebugSessionHandler(watcherContext)
                    .handle(new DebugSessionMessage(DebugSessionStatus.ON, "com.watcher.sandbox"))
                    .join();

            new SetBreakpointHandler(watcherContext)
                    .handle(new SetBreakpointMessage(Breakpoint.builder()
                            .classCanonicalName(clazz.getCanonicalName())
                            .line(line)
                            .build()))
                    .join();

            return (T) instance;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
