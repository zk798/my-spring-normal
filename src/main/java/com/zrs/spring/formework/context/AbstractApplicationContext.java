package com.zrs.spring.formework.context;

public abstract class AbstractApplicationContext {

    protected void onRefresh(){}


    protected abstract void refreshBeanFactory();

}
