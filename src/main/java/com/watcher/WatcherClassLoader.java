package com.watcher;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Marcin Bukowiecki
 */
public class WatcherClassLoader extends ClassLoader {

    private final Map<String, Class> loadedClasses = new HashMap<>();

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    public Class defineClass(String name, byte[] b) {
        Class result = getLoadedClass(name);

        try {
            if (result == null) {
                result = defineClass(name, b, NumberUtils.INTEGER_ZERO, b.length);
            }
        } catch (Error e) {
            System.err.println(name);
            throw e;
        }

        loadedClasses.put(name, result);
        return result;
    }

    public Map<String, Class> getLoadedClasses() {
        return loadedClasses;
    }

    public Class<?> getLoadedClass(String name) {
        return findLoadedClass(name);
    }

    public Optional<Class<?>> getLoadedClassOpt(String name) {
        return Optional.ofNullable(findLoadedClass(name));
    }

}
