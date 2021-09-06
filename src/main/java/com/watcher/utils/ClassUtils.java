package com.watcher.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * @author Marcin Bukowiecki
 */
public class ClassUtils {

    public static Optional<String> getClassCanonicalName(final Class<?> aClass) {
        try {
            String canonicalName = aClass.getCanonicalName();
            if (StringUtils.isEmpty(canonicalName)) {
                canonicalName = aClass.getName();
            }
            return Optional.of(canonicalName);
        } catch (Throwable e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
