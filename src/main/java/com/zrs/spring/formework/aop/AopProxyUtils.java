package com.zrs.spring.formework.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class AopProxyUtils {

    public static Object getTargetObject(Object proxy){

        if(!isProxyObject(proxy)){
            return proxy;
        }
        try {
            return getProxyTargetObject(proxy);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getProxyTargetObject(Object proxy) throws NoSuchFieldException, IllegalAccessException {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy = (AopProxy)h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(aopProxy);
    }

    private static boolean isProxyObject(Object proxy){
        return Proxy.isProxyClass(proxy.getClass());
    }
}
