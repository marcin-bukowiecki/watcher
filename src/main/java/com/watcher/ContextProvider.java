package com.watcher;

import io.vertx.core.eventbus.MessageCodec;

/**
 * @author Marcin Bukowiecki
 */
public interface ContextProvider {

    <T> MessageCodec<T, T> getCodec(Class<?> forClass);
}
