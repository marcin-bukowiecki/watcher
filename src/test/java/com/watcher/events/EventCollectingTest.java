package com.watcher.events;

import com.watcher.AbstractTestCase;
import com.watcher.service.ThreadLocalCollector;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sandbox.events.Main1;
import sandbox.events.TestLoop1;

import java.util.Collections;
import java.util.List;

/**
 * @author Marcin Bukowiecki
 */
public class EventCollectingTest extends AbstractTestCase {

    @Test
    public void collectEvents_1() {
        watcherContext.getWatcherSession().setCollectExceptionData(true);
        watcherContext.getWatcherSession().getTransformContext().setSupportedPackages(Collections.singleton("sandbox.events"));

        var testClass = Main1.class; //invoke class loader

        try {
            Main1.main(null);
        } catch (NullPointerException ignored) { }

        List<BaseEvent> events = ThreadLocalCollector.THREAD_LOCAL.get().chopEvents();
        Assertions.assertThat(events)
                .hasSize(7);
        events.forEach(System.out::println);
    }

    @Test
    public void collectEvents_2() {
        watcherContext.getWatcherSession().setCollectExceptionData(true);
        watcherContext.getWatcherSession().getTransformContext().setSupportedPackages(Collections.singleton("sandbox.events"));

        var testClass = TestLoop1.class; //invoke class loader

        try {
            new TestLoop1().foo();
        } catch (RuntimeException ignored) { }

        List<BaseEvent> events = ThreadLocalCollector.THREAD_LOCAL.get().chopEvents();
        events.forEach(System.out::println);
        Assertions.assertThat(events)
                .hasSize(7);
    }
}
