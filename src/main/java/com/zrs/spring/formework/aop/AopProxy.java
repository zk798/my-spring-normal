package com.zrs.spring.formework.aop;

import lombok.Setter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AopProxy implements InvocationHandler {

    @Setter
    private AopConfig config;

    private Object target;

    public Object getProxy(Object instance){
        this.target = instance;
        Class<?> aClass = instance.getClass();
        Object proxyInstance = Proxy.newProxyInstance(aClass.getClassLoader(), aClass.getInterfaces(), this);
        return proxyInstance;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(),method.getParameterTypes());
        if(config.contains(m)){
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        Object invoke = method.invoke(this.target, args);

        if(config.contains(m)){
            AopConfig.Aspect aspect = config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }
        return invoke;
    }
}
