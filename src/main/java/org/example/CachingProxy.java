package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CachingProxy implements InvocationHandler, ApplicationProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingProxy.class);
    private final Object target;
    private final ConcurrentHashMap<Object, Object> cache;

    private CachingProxy(Object target) {
        this.cache = new ConcurrentHashMap<>();
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("Proxy method {} has been invoked with arguments {}", method.getName(), args);
        if (!targetMethod(method).isAnnotationPresent(Cacheable.class)) {
            return method.invoke(target, args);
        }

        final String cacheKey = cacheKeyFromArguments(args);
        return cache.computeIfAbsent(cacheKey, (key) -> {
            try {
                LOGGER.info("--> Cache not populated. Calling method and populating cache");
                return method.invoke(target, args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Method targetMethod(Method method) throws NoSuchMethodException {
        // is already a proxy
        if (Proxy.isProxyClass(target.getClass()) && Proxy.getInvocationHandler(target) instanceof ApplicationProxy appProxy) {
            return appProxy.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        }
        // is not a proxy
        return target.getClass().getMethod(method.getName(), method.getParameterTypes());
    }

    private static String cacheKeyFromArguments(Object[] args) {
        return Arrays.stream(args)
                .map(Object::toString)
                .collect(Collectors.joining("_"));
    }

    /*
     * Proxy is part of the Java Reflection library.
     */
    public static AccountService newInstance(Object obj) {
        return (AccountService) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new CachingProxy(obj));
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
