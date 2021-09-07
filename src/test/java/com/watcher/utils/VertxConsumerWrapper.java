package com.watcher.utils;

import io.vertx.core.eventbus.MessageConsumer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcin Bukowiecki
 */
public class VertxConsumerWrapper {

    private final MessageConsumer<?> consumer;

    public VertxConsumerWrapper(MessageConsumer<?> consumer) {
        this.consumer = consumer;
    }

    public void unregister() {
        CountDownLatch flag = new CountDownLatch(1);
        consumer.unregister(result -> flag.countDown());

        try {
            if (!flag.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Didn't unregister");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
