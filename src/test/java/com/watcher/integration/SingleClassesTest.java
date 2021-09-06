package com.watcher.integration;

import com.watcher.WatcherContext;
import com.watcher.events.GetLocalEvent;
import com.watcher.model.BreakpointData;
import com.watcher.sandbox.BranchingClassTest;
import com.watcher.sandbox.IntAdder;
import com.watcher.sandbox.Main;
import com.watcher.service.CollectingContext;
import com.watcher.service.ThreadLocalCollector;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcin Bukowiecki
 */
public class SingleClassesTest extends AbstractIntegrationTest {

    @Test
    public void setBreakpointForRepeatedMethod() throws InterruptedException {
        Main instance = AbstractIntegrationTest.setBreakpoint(Main.class, 37);
        CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(instance::loop);
        TimeUnit.SECONDS.sleep(15L);
        instance.run = false;
    }

    @Ignore
    @Test
    public void setBreakpointForIntAdder() {
        IntAdder instance = AbstractIntegrationTest.setBreakpoint(IntAdder.class, 6);
        var adder = instance.adder(5, 23);
        Assertions.assertThat(adder)
                .isEqualTo(28);
        //TODO assert
        WatcherContext.getInstance().getVertx().eventBus().consumer("debug.data", msg -> {
            BreakpointData body = (BreakpointData) msg.body();
            Assertions.assertThat(body.getData())
                    .hasSize(1);
        });
    }

    @Ignore
    @Test
    public void setBreakpointForIfElse() {
        BranchingClassTest instance = AbstractIntegrationTest.setBreakpoint(BranchingClassTest.class, 7);
        var result = instance.simpleIfElse(123);
        Assertions.assertThat(result)
                .isEqualTo("foo");
        Set<CollectingContext> collectingContexts = watcherContext.collectingContext();
        Assertions.assertThat(collectingContexts)
                .hasSize(1);
        Assertions.assertThat(collectingContexts.iterator().next().chopEvents())
                .hasSize(2)
                .hasOnlyElementsOfType(GetLocalEvent.class);
    }
}
