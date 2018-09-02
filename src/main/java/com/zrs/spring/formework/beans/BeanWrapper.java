package com.zrs.spring.formework.beans;

import lombok.Getter;
import lombok.Setter;

@Getter
public class BeanWrapper {

    /**
     * 支持相应 有一个监听
     */
    @Setter
    private BeanPostProcessor beanPostProcessor;

    private Object warpperInstance;
    private Object originalInstance;

    public BeanWrapper(Object instance){
        this.warpperInstance = instance;
        this.originalInstance = instance;
    }
}
