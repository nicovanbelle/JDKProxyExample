package org.example.proxy;

import org.example.annotation.Cacheable;
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

    /**
     *
     */
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

    /**
     * Return the method of the target as this is the one that is annotated.
     * The methods of the proxy are not annotated.
     * As multiple proxies can be wrapped we need to loop until we find the class that's not a proxy itself.
     */
    private Method targetMethod(Method method) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Object wrappedTarget = target;
        while (Proxy.isProxyClass(targetClass) && Proxy.getInvocationHandler(wrappedTarget) instanceof ApplicationProxy appProxy) {
            targetClass = appProxy.getTarget().getClass();
            wrappedTarget = appProxy.getTarget();
        }
        return targetClass.getMethod(method.getName(), method.getParameterTypes());
    }

    private static String cacheKeyFromArguments(Object[] args) {
        return Arrays.stream(args)
                .map(Object::toString)
                .collect(Collectors.joining("_"));
    }

    /*
     * Proxy is part of the Java Reflection library.
     */
    public static <T> T newInstance(Object obj) {
        return (T) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new CachingProxy(obj));
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
