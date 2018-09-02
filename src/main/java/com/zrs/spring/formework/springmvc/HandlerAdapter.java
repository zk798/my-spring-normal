package com.zrs.spring.formework.springmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {

    private  Map<String, Integer> paramterMap;

    public HandlerAdapter( Map<String, Integer> paramterMap ){
        this.paramterMap = paramterMap;
    }

    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp , HandlerMapping handler){

        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

        Object[] paramterValues= new Object[paramterMap.size()];
        for (Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            String k = entry.getKey();
            String v = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s",",");
            if(!paramterMap.containsKey(k)){
                continue;
            }
            Integer index = paramterMap.get(k);

            paramterValues[index] = caseStringValue(v, parameterTypes[index]);
        }

        Integer index = paramterMap.get(HttpServletRequest.class.getTypeName());
        if(index != null){
            paramterValues[index] =req;
        }
        index = paramterMap.get(HttpServletResponse.class.getTypeName());
        if(index != null){
            paramterValues[index] =resp;
        }

        try {
            Object invoke = handler.getMethod().invoke(handler.getController(), paramterValues);
            if(invoke == null){
                return null;
            }
            boolean isModelAndView = handler.getMethod().getReturnType() == ModelAndView.class;
            if(isModelAndView){
                return (ModelAndView)invoke;
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }


        return null;
    }

    private Object caseStringValue(String v, Class<?> clazz) {
        if(v==null || "".equals(v.trim())){
            return null;
        }
        if(clazz == String.class){
            return v;
        }
        if(clazz == int.class){
            return Integer.valueOf(v).intValue();
        }
        if(clazz == Integer.class){
            return Integer.valueOf(v);
        }
        return v;
    }

}
