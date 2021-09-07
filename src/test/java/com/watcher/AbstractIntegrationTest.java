package com.watcher;

import com.watcher.agent.WatcherClassTransformer;
import com.watcher.api.WatcherApi;
import com.watcher.handlers.DebugSessionHandler;
import com.watcher.handlers.SetBreakpointHandler;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.Breakpoint;
import com.watcher.model.BreakpointData;
import com.watcher.service.CollectingContext;
import com.watcher.service.ThreadLocalCollector;
import com.watcher.utils.VertxConsumerWrapper;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public abstract class AbstractIntegrationTest {

    protected static WatcherContext watcherContext;

    @BeforeClass
    public static void setupContext() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        watcherContext = new WatcherContext(instrumentation, new WatcherContextProvider());
        WatcherApi.init(watcherContext);
        WatcherClassTransformer watcherClassTransformer = new WatcherClassTransformer(watcherContext);
        watcherContext.getInstrumentation().addTransformer(watcherClassTransformer, true);
        watcherContext.printASM = true;
    }

    @Before
    public void setup() {
        ThreadLocalCollector.collectingRegister.clear();
        final CollectingContext collectingContext = ThreadLocalCollector.THREAD_LOCAL.get();
        if (collectingContext != null) {
            collectingContext.chopEvents();
        }

        stopDebugSession();

        watcherContext.flushAll();
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

    public static void stopDebugSession() {
        new DebugSessionHandler(watcherContext)
                .handle(new DebugSessionMessage(DebugSessionStatus.OFF, "com.watcher.sandbox"))
                .join();
    }

    public VertxConsumerWrapper registerConsumer(final CountDownLatch flag, final Consumer<BreakpointData> assertion) {
        return new VertxConsumerWrapper(WatcherContext.getInstance().getVertx().eventBus().consumer("debug.data", msg -> {
            BreakpointData body = (BreakpointData) msg.body();
            log.info("Breakpoint data: {}", body);
            assertion.accept(body);
            flag.countDown();
        }));
    }
}
