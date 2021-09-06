package com.watcher;

import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.AllArguments;
import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.Origin;
import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.assertj.core.internal.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class Interceptor {

    @RuntimeType
    public Object intercept(@SuperCall Callable<?> callable,
                            @AllArguments Object[] allArguments,
                            @Origin Method method,
                            @Origin Class clazz) throws Exception {
        try {
            return callable.call();
        } catch (Throwable t) {
            throw t;
        }
    }
}
