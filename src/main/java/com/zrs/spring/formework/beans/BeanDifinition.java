package com.zrs.spring.formework.beans;

/**
 * 对配置进行包装
 * @author zrs
 */
public class BeanDifinition {

    private String beanClassName;
    private String factoryBeanName;
    private Boolean isLazyInit;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public Boolean getLazyInit() {
        return isLazyInit;
    }

    public void setLazyInit(Boolean lazyInit) {
        isLazyInit = lazyInit;
    }
}
