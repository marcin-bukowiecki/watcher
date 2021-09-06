package com.watcher;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * @author Marcin Bukowiecki
 *
 * Vertx Codec context provider
 */
public class WatcherContextProvider implements ContextProvider {

    /**
     * Get mVertx message codex
     *
     * @param forClass codec for message type
     * @param <T> type of message
     * @return Vertx {@link MessageCodec}
     */
    @Override
    public <T> MessageCodec<T, T> getCodec(Class<?> forClass) {
        return new MessageCodec<T, T>() {
            @Override
            public void encodeToWire(Buffer buffer, T o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public T decodeFromWire(int i, Buffer buffer) {
                throw new UnsupportedOperationException();
            }

            @Override
            public T transform(T o) {
                return o;
            }

            @Override
            public String name() {
                return forClass.getCanonicalName() + "codec";
            }

            @Override
            public byte systemCodecID() {
                return -1;
            }
        };
    }
}
