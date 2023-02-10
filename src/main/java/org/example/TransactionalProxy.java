package org.example;

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

    private Method targetMethod(Method method) throws NoSuchMethodException {
        // is already a proxy
        if (Proxy.isProxyClass(target.getClass())
                && Proxy.getInvocationHandler(target) instanceof ApplicationProxy appProxy) {
            return appProxy.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
        }
        // is not a proxy
        return target.getClass().getMethod(method.getName(), method.getParameterTypes());
    }

    /*
     * Java Reflection.
     * Can in some way be compared to AST in js (Abstract Syntax Tree) where we can fetch information from code constructs.
     * What interfaces does a class have?
     * What argument or return types or does a method have?
     * Can also be used to instantiate objects, set private fields, etc.
     */
    public static AccountService newInstance(Object obj) {
        return (AccountService) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                new TransactionalProxy(obj));
    }

    @Override
    public Object getTarget() {
        return target;
    }
}
