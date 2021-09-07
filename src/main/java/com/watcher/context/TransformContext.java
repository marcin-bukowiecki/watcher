package com.watcher.context;

import com.watcher.WatcherContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Marcin Bukowiecki
 *
 * Class for hodling transformation information
 */
@Slf4j
public class TransformContext {

    //Package format is: com/acme, com/acme/foo etc.
    private volatile Set<String> supportedPackages = new HashSet<>();

    public TransformContext() {
        log.info("Created transform context");
        if (!supportedPackages.isEmpty()) {
            log.info("Got supported packages:");
            for (String supportedPackage : supportedPackages) {
                log.info(supportedPackage);
            }
        }
    }

    /**
     * This method sets the supported packages collection. If a class belongs to a supported package it will be transformed.
     *
     * @param supportedPackages collection of supported packages
     */
    public void setSupportedPackages(Set<String> supportedPackages) {
        supportedPackages = supportedPackages.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        if (WatcherContext.logEnabled()) {
            log.info("Got supported packages");
            supportedPackages.forEach(log::info);
        }
        this.supportedPackages = supportedPackages.stream().map(p -> p.replace('/', '.')).collect(Collectors.toSet());
    }

    /**
     * Method checks by class name if given class can be transformed
     *
     * @param className given class name
     * @return true if class can be transformed, false otherwise
     */
    public boolean isToTransform(String className) {
        if (StringUtils.isEmpty(className)) {
            return false;
        }
        if (CollectionUtils.isEmpty(this.supportedPackages)) {
            return false;
        }

        className = className.replace('/', '.');

        if (className.contains("$")) { //nested/inner classes are not supported
            return false;
        }
        String mainPackage = WatcherContext.getInstance().getMainPackage();
        if (StringUtils.isNotEmpty(mainPackage) && className.startsWith(mainPackage)) {
            if (WatcherContext.logEnabled()) {
                log.info("{} matched -Dwatcher.main.package", className);
            }
            return true;
        }
        boolean result = supportedPackages.stream().anyMatch(className::startsWith);
        if (result) {
            if (WatcherContext.logEnabled()) {
                log.info("{} matched and is for transformation, matcher: {}", className, String.join(",", supportedPackages));
            }
        }
        return result;
    }

    public Set<String> getSupportedPackages() {
        return Collections.unmodifiableSet(supportedPackages);
    }
}
