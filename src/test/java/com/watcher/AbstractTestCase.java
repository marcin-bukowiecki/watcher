package com.watcher;

import com.watcher.agent.WatcherClassTransformer;
import com.watcher.api.WatcherApi;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.assertj.core.internal.bytebuddy.agent.builder.AgentBuilder;
import org.assertj.core.internal.bytebuddy.dynamic.DynamicType;
import org.assertj.core.internal.bytebuddy.implementation.MethodDelegation;
import org.assertj.core.internal.bytebuddy.matcher.ElementMatchers;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author Marcin Bukowiecki
 */
public abstract class AbstractTestCase {

    protected static WatcherContext watcherContext;

    @BeforeClass
    public static void setup() {
        ByteBuddyAgent.install();
        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
        watcherContext = new WatcherContext(instrumentation, new WatcherContextProvider());
        WatcherApi.init(watcherContext);
        WatcherContext.getInstance().printASM = false;
        instrumentation.addTransformer(new WatcherClassTransformer(watcherContext), true);

        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("com.watcher"))
                .transform((builder, typeDescription, classLoader, m) -> builder
                        .method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(new Interceptor())))
                .installOn(instrumentation);
    }
}
