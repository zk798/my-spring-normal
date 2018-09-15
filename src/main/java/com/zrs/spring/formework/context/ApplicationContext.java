package com.zrs.spring.formework.context;

import com.zrs.spring.formework.annotation.Autowried;
import com.zrs.spring.formework.annotation.Controller;
import com.zrs.spring.formework.annotation.Service;
import com.zrs.spring.formework.aop.AopConfig;
import com.zrs.spring.formework.beans.BeanDifinition;
import com.zrs.spring.formework.beans.BeanPostProcessor;
import com.zrs.spring.formework.beans.BeanWrapper;
import com.zrs.spring.formework.context.support.BeanDifinitionReader;
import com.zrs.spring.formework.core.BeanFactory;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    public String[] configLocations;

    @Getter
    BeanDifinitionReader beanDifinitionReader;

    Map<String,BeanDifinition> beanDifinitionMap = new HashMap<>();

    Map<String,Object> singleBeanCacheMap = new HashMap<>();

    Map<String,BeanWrapper> BeanWrapperMap = new ConcurrentHashMap<>();

    public ApplicationContext(String... configLocations){
        this.configLocations = configLocations;
        this.refresh();
    }

    public void refresh(){

        //定位
        beanDifinitionReader = new BeanDifinitionReader(configLocations);

        //加载
        List<String> beanDefinitions = beanDifinitionReader.loadBeanDefinitions();

        //注册
        doRegisty(beanDefinitions);

        //注入
        doAutoWrited();



        System.out.println(1);
    }

    /**
     * 自动化的依赖注入
     */
    private void doAutoWrited() {
        for (Map.Entry<String, BeanDifinition> entry : beanDifinitionMap.entrySet()) {

            String beanName = entry.getKey();
            BeanDifinition beanDifinition = entry.getValue();
            if(beanDifinition.getLazyInit()){
                continue;
            }
            Object bean = getBean(beanName);
            System.out.println(bean.getClass());

        }
    }

    private void pupolateBean(Object instance,String beanName){

        Class<?> aClass = instance.getClass();
//        if(!aClass.isAnnotationPresent(Controller.class) && !aClass.isAnnotationPresent(Service.class)){
//            return;
//        }

        //获取所有属性 进行注入
        Field[] fields = aClass.getDeclaredFields();

        for (Field field : fields) {

            if(!field.isAnnotationPresent(Autowried.class)){
                continue;
            }
            Autowried annotation = field.getAnnotation(Autowried.class);
            String name = annotation.value().trim();
            if("".equals(name)){
                name = field.getType().getName();
            }

            if(!BeanWrapperMap.containsKey(name)) {
                getBean(name);
            }

            Object o = BeanWrapperMap.get(name);
            field.setAccessible(true);
            try {
                field.set(instance, ((BeanWrapper) o).getWarpperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }


    }



    private void doRegisty(List<String> beanDefinitions) {
        beanDefinitions.forEach(beanName->{
            try {
                Class<?> clazz = Class.forName(beanName);

                //接口
                if(clazz.isInterface()){
                    return;
                }

                BeanDifinition beanDifinition = beanDifinitionReader.registerBean(beanName);
                if(beanDifinition != null){
                    beanDifinitionMap.put(beanDifinition.getBeanClassName(),beanDifinition);
                }

                //将所有实现类的接口也注册
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    beanDifinitionMap.put(anInterface.getName(),beanDifinition);
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        });
    }

    @Override
    public Object getBean(String beanName) {

        BeanDifinition beanDifinition = this.beanDifinitionMap.get(beanName);

        BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
        Object instance = getInstance(beanDifinition);


        //before
        beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
        BeanWrapper beanWrapper = new BeanWrapper(instance);
        try {
            beanWrapper.setAopConfig(instantionAopConfig(beanDifinition));
        } catch (Exception e) {
            e.printStackTrace();
        }
        beanWrapper.setBeanPostProcessor(beanPostProcessor);
        BeanWrapperMap.put(beanName,beanWrapper);

        pupolateBean(instance,beanName);

        //after
        beanPostProcessor.postProcessAfterInitialization(instance,beanName);

        return BeanWrapperMap.get(beanName).getWarpperInstance();

    }

    private Object getInstance(BeanDifinition beanDifinition){
        String beanClassName = beanDifinition.getBeanClassName();
        if(singleBeanCacheMap.containsKey(beanClassName)){
            return singleBeanCacheMap.get(beanClassName);
        }else{
            try {
                Class<?> aClass = Class.forName(beanClassName);
                Object instance = aClass.newInstance();
                singleBeanCacheMap.put(beanClassName,instance);
                return instance;

            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getBeanDefinitionCount() {
        return beanDifinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
       return beanDifinitionMap.keySet().toArray(new String[0]);
    }

    private AopConfig instantionAopConfig(BeanDifinition beanDifinition) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException {

        AopConfig config =new AopConfig();
        String expression = beanDifinitionReader.getConfig().getProperty("pointCut");
        String[] before = beanDifinitionReader.getConfig().getProperty("aspectBefore").split("\\s");
        String[] after = beanDifinitionReader.getConfig().getProperty("aspectAfter").split("\\s");
        Class<?> clazz = Class.forName(beanDifinition.getBeanClassName());
        Class<?> aspectClass = Class.forName(before[0]);

        Pattern compile = Pattern.compile(expression);
        for (Method m : clazz.getMethods()) {
            if(compile.matcher(m.toString()).matches()){
                config.put(m,new Method[]{aspectClass.getMethod(before[1]),aspectClass.getMethod(after[1])},aspectClass.newInstance());
            }
        }

        return config;

    }

}
