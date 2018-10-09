package com.zrs.spring.demo.aspect;

public class LogAspect {
    public void before(){
        System.out.println("before");
    }
    public void after(){
        System.out.println("after");
    }

}
