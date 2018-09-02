package com.zrs.spring.formework.beans;

import com.zrs.spring.formework.core.FactoryBean;

public class BeanPostProcessor extends FactoryBean {


    public void postProcessBeforeInitialization(Object bean,String beanName){}
    public void postProcessAfterInitialization(Object bean,String beanName){}

}
