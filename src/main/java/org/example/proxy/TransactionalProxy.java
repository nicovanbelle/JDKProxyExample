package org.example.proxy;

import org.example.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TransactionalProxy implements InvocationHandler, ApplicationProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalProxy.class);
    private final Object target;
    private final TransactionManager transactionManager;

    private TransactionalProxy(Object target) {
        this.target = target;
        this.transactionManager = new TransactionManager();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("Proxy method {} has been invoked with arguments {}", method.getName(), args);
        if (!targetMethod(method).isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        transactionManager.start();
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit();
            return result;
        } catch (Exception e) {
            transactionManager.rollback();
            throw e;
        }
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

    /*
     * Java Reflection.
     * Can in some way be compared to AST in js (Abstract Syntax Tree) where we can fetch information from code constructs.
     * What interfaces does a class have?
     * What argument or return types or does a method have?
     * Can also be used to instantiate objects, set private fields, etc.
     */
    public static<T> T newInstance(Object obj) {
        return (T) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new TransactionalProxy(obj));
    }

    @Override
    public Object getTarget() {
        return target;
    }

    private static class  TransactionManager {
        private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

        public void start() {
            LOGGER.info("---> Starting Transaction");
        }

        public void commit() {
            LOGGER.info("---> Commit Transaction");
        }

        public void rollback() {
            LOGGER.info("---> Rollback Transaction");
        }
    }
}
