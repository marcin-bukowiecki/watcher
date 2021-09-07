package com.watcher.agent;

import com.watcher.WatcherContext;
import com.watcher.WatcherContextProvider;
import com.watcher.api.WatcherApi;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

/**
 * @author Marcin Bukowiecki
 *
 * Entry point for setting up Watcher Agent
 */
public class WatcherAgent {

    public static void agentmain(String agentArgs, Instrumentation instrumentation) throws IOException {
        //Problem with dependency conflicts :))
        /*
        JarFile jarFile = new JarFile("C:\\Users\\MB\\.m2\\repository\\io\\vertx\\vertx-core\\3.9.1\\vertx-core-3.9.1.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\netty-resolver-4.1.49.Final.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\netty-all-4.0.56.Final.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\netty-all-4.0.56.Final.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\asm-7.1.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\asm-7.1.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\commons-collections-3.2.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\commons-lang3-3.8.1.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        jarFile = new JarFile("H:\\watcher-test\\lib\\javaparser-core-2.0.0.jar");
        instrumentation.appendToSystemClassLoaderSearch(jarFile);
        */
        premain(agentArgs, instrumentation);
    }

    /**
     * Entry point for starting Watcher Agent by JVM
     *
     * @param agentArgs agent arguments are omitted (agent uses system properties)
     * @param inst {@link Instrumentation} instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        try {
            System.out.println("Program running with Watcher Agent version 1.0.1");
            var instance = WatcherContext.init(inst, new WatcherContextProvider());
            System.out.println("Using " + instance.getSecurityProvider().info() + " security provider");
            WatcherApi.init(instance);
            final WatcherClassTransformer watcherClassTransformer = new WatcherClassTransformer(instance);
            inst.addTransformer(watcherClassTransformer, true);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }
}
