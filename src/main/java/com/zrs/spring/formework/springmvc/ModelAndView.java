package com.zrs.spring.formework.springmvc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModelAndView {

    private String viewName;
    private Map<String,?> model;

}
