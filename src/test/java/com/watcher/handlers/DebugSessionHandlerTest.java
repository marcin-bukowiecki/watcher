package com.watcher.handlers;

import com.watcher.AbstractTestCase;
import com.watcher.DebugSessionStatus;
import com.watcher.LoadedClassContext;
import com.watcher.StackValueHandler;
import com.watcher.WatcherContext;
import com.watcher.asm.WatcherAsmClassValidator;
import com.watcher.messages.DebugSessionMessage;
import com.watcher.messages.SetBreakpointMessage;
import com.watcher.model.Breakpoint;
import com.watcher.model.BreakpointData;
import com.watcher.model.BreakpointStatus;
import com.watcher.sandbox.Test2;
import com.watcher.sandbox.Test3;
import com.watcher.sandbox.Test4;
import com.watcher.utils.BreakpointMatcher;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collections;

/**
 * @author Marcin Bukowiecki
 */
public class DebugSessionHandlerTest extends AbstractTestCase {

    @Test
    public void shouldSetBreakpointForLoadedClass() {
        var testClass = Test2.class; //invoke class loader

        new DebugSessionHandler(watcherContext)
                .handle(new DebugSessionMessage(DebugSessionStatus.ON, "com.watcher.sandbox"))
                .join();

        WatcherContext.getInstance().printASM = true;
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
