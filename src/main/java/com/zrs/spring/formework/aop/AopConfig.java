package com.zrs.spring.formework.aop;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AopConfig {
    private Map<Method,Aspect> points = new HashMap<>();

    public void put(Method target, Method[] methods, Object aspect){
        points.put(target, new Aspect(aspect,methods));
    }

    public Aspect get(Method method){
        return points.get(method);
    }
    public Boolean contains(Method method){
        return points.containsKey(method);
    }


    @Setter
    @Getter
    @AllArgsConstructor
    public class Aspect{
        private  Object aspect;
        private Method[] points;
    }


}
