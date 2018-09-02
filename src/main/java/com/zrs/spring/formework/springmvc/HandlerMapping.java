package com.zrs.spring.formework.springmvc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

@Setter
@Getter
@AllArgsConstructor
public class HandlerMapping {

    private Object controller;
    private Method method;
    private Pattern pattern;
}
