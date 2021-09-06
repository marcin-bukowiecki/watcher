package com.watcher.events.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherArgsSerializer extends JsonSerializer<Object[]> {

    @Override
    public void serialize(Object[] obj, JsonGenerator gen, SerializerProvider serializerProvider) throws IOException {
        gen.writeArrayFieldStart("args");
        gen.writeStartArray();
        for (Object o : obj) {
            gen.writeString(String.valueOf(o));
        }
        gen.writeEndArray();
    }
}
