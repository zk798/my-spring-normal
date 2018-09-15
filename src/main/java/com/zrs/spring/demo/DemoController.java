package com.zrs.spring.demo;

import com.zrs.spring.demo.service.DemoService;
import com.zrs.spring.formework.annotation.Autowried;
import com.zrs.spring.formework.annotation.Controller;
import com.zrs.spring.formework.annotation.RequestMapping;
import com.zrs.spring.formework.annotation.RequestParameter;
import com.zrs.spring.formework.springmvc.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/p")
public class DemoController {

    @Autowried
    private DemoService demoService;

    @RequestMapping("/v")
    public ModelAndView getName(@RequestParameter("name")String name){

        demoService.get();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index.html");
        Map<String,Object> map = new HashMap<>();
        map.put("name",name);
        modelAndView.setModel(map);
        return modelAndView;

    }
}
