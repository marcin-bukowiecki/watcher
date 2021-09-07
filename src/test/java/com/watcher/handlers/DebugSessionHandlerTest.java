package com.watcher.handlers;

import com.watcher.*;
import com.watcher.asm.WatcherAsmClassValidator;
import com.watcher.events.GetLocalEvent;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.Breakpoint;
import com.watcher.model.BreakpointData;
import com.watcher.model.BreakpointStatus;
import com.watcher.sandbox.*;
import com.watcher.service.CollectingContext;
import com.watcher.service.ThreadLocalCollector;
import com.watcher.utils.BreakpointMatcher;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 */
@Slf4j
public class DebugSessionHandlerTest extends AbstractIntegrationTest {

    @Test
    public void setBreakpointForRepeatedMethod() throws InterruptedException {
        Main instance = AbstractIntegrationTest.setBreakpoint(Main.class, 37);
        CompletableFuture.runAsync(instance::loop);
        log.info("Running unit test for 3 seconds...");
        TimeUnit.SECONDS.sleep(3L);
        instance.run = false;
        final Set<CollectingContext> collectingRegister = ThreadLocalCollector.collectingRegister;
        collectingRegister.stream()
                .findFirst()
                .map(result -> Assertions.assertThat(result.chopEvents()).hasSize(6))
                .orElseThrow(() -> new AssertionError("Expected collection result"));
    }

    @Test
    public void setBreakpointForIntAdder() throws InterruptedException {
        var flag = new CountDownLatch(1);
        var consumer = registerConsumer(flag, breakpointData -> Assertions
                .assertThat(breakpointData.getData().stream().filter(it -> it instanceof GetLocalEvent).collect(Collectors.toList()))
                .hasSize(2));

        try {
            IntAdder instance = AbstractIntegrationTest.setBreakpoint(IntAdder.class, 7);
            var adder = instance.adder(5, 23);
            Assertions.assertThat(adder)
                    .isEqualTo(28);
            Assert.assertTrue(flag.await(2, TimeUnit.SECONDS));
        } finally {
            consumer.unregister();
        }
    }

    @Test
    public void setBreakpointForIfElse() throws InterruptedException {
        var flag = new CountDownLatch(1);
        var consumer = registerConsumer(flag, breakpointData -> Assertions
                .assertThat(breakpointData.getData().stream().filter(it -> it instanceof GetLocalEvent).collect(Collectors.toList()))
                .hasSize(1));

        try {
            BranchingClassTest instance = AbstractIntegrationTest.setBreakpoint(BranchingClassTest.class, 7);
            var result = instance.simpleIfElse(123);
            Assertions.assertThat(result)
                    .isEqualTo("foo");

            Assert.assertTrue(flag.await(2, TimeUnit.SECONDS));
        } finally {
            consumer.unregister();
        }
    }

    @Test
    public void shouldSetBreakpointForLoadedClass() {
        var testClass = Test2.class; //invoke class loader

        new DebugSessionHandler(watcherContext)
                .handle(new DebugSessionMessage(DebugSessionStatus.ON, "com.watcher.sandbox"))
                .join();

        new SetBreakpointHandler(watcherContext)
                .handle(new SetBreakpointMessage(Breakpoint.builder().classCanonicalName(testClass.getCanonicalName()).line(24).build()))
                .join();

        LoadedClassContext loadedClassContext = watcherContext.getLoadedClassContext(testClass.getCanonicalName());
        byte[] actualBytecode = loadedClassContext.getActualBytecode();

        var classMatcher = new WatcherAsmClassValidator(testClass.getCanonicalName(),
                Collections.singletonList(Breakpoint.builder().classCanonicalName(testClass.getCanonicalName()).line(24).build()));
        new ClassReader(actualBytecode).accept(classMatcher, 0);

        Assertions.assertThat(classMatcher.getExpectedBreakpoints())
                .allMatch(BreakpointMatcher::isMatched);
    }

    @Test
    public void shouldSetBreakpointForUnloadedClass() {
        new DebugSessionHandler(watcherContext)
                .handle(new DebugSessionMessage(DebugSessionStatus.ON, "com.watcher.sandbox"))
                .join();

        new SetBreakpointHandler(watcherContext)
                .handle(new SetBreakpointMessage(Breakpoint.builder().classCanonicalName("com.watcher.sandbox.Test3")
                        .line(6).build()))
                .join();

        var testClass = Test3.class; //invoke class loader

        LoadedClassContext loadedClassContext = watcherContext
                .getLoadedClassContext("com.watcher.sandbox.Test3");
        Assertions.assertThat(loadedClassContext.copyBreakpoints())
                .allMatch(p -> p.getStatus() == BreakpointStatus.active);

        var classMatcher = new WatcherAsmClassValidator("com.watcher.sandbox.Test3",
                Collections.singletonList(Breakpoint.builder().classCanonicalName("com.watcher.sandbox.Test3")
                        .line(6).build()));
        new ClassReader(loadedClassContext.getActualBytecode()).accept(classMatcher, 0);

        Assertions.assertThat(classMatcher.getExpectedBreakpoints())
                .allMatch(BreakpointMatcher::isMatched);
    }

    @Test
    public void shouldSetBreakpointAfterDebugSessionStart() throws Throwable {
        var testClass = Test4.class; //invoke class loader

        new SetBreakpointHandler(watcherContext)
                .handle(new SetBreakpointMessage(Breakpoint.builder().classCanonicalName("com.watcher.sandbox.Test4")
                        .line(6)
                        .build()))
                .join();
        var s = StackValueHandler.class;
        new DebugSessionHandler(watcherContext)
                .handle(new DebugSessionMessage(DebugSessionStatus.ON, "com.watcher.sandbox"))
                .join();

        LoadedClassContext loadedClassContext = watcherContext
                .getLoadedClassContext("com.watcher.sandbox.Test4");
        Assertions.assertThat(loadedClassContext.copyBreakpoints())
                .allMatch(p -> p.getStatus() == BreakpointStatus.active);

        var classMatcher = new WatcherAsmClassValidator("com.watcher.sandbox.Test4",
                Collections.singletonList(Breakpoint.builder().classCanonicalName("com.watcher.sandbox.Test4")
                        .line(6)
                        .build()));
        new ClassReader(loadedClassContext.getActualBytecode()).accept(classMatcher, 0);

        Assertions.assertThat(classMatcher.getExpectedBreakpoints())
                .allMatch(BreakpointMatcher::isMatched);

        WatcherContext.getInstance().getVertx().eventBus().consumer("debug.data", msg -> {
            BreakpointData body = (BreakpointData) msg.body();
            Assertions.assertThat(body.getData())
                    .hasSize(1);
        });
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle testThrow = lookup.findStatic(testClass, "testThrow",
                MethodType.methodType(void.class, Integer.class));
        try {
            testThrow.invoke(10);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
