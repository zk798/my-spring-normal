package com.zrs.spring.formework.beans;

import com.zrs.spring.formework.aop.AopConfig;
import com.zrs.spring.formework.aop.AopProxy;
import lombok.Getter;
import lombok.Setter;

@Getter
public class BeanWrapper {

    /**
     * 支持响应 有一个监听
     */
    @Setter
    private BeanPostProcessor beanPostProcessor;

    private Object warpperInstance;
    private Object originalInstance;
    AopProxy aopProxy = new AopProxy();

    public BeanWrapper(Object instance){
        this.warpperInstance = aopProxy.getProxy(instance);
        this.originalInstance = instance;
    }

    public void setAopConfig(AopConfig config){
        aopProxy.setConfig(config);
    }
}
