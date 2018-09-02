package com.zrs.spring.formework.springmvc.servlet;

import com.zrs.spring.formework.annotation.Controller;
import com.zrs.spring.formework.annotation.RequestMapping;
import com.zrs.spring.formework.annotation.RequestParameter;
import com.zrs.spring.formework.context.ApplicationContext;
import com.zrs.spring.formework.springmvc.HandlerAdapter;
import com.zrs.spring.formework.springmvc.HandlerMapping;
import com.zrs.spring.formework.springmvc.ModelAndView;
import com.zrs.spring.formework.springmvc.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Administrator
 */
@WebServlet(loadOnStartup = 1,urlPatterns = "/*",initParams = {@WebInitParam( name="contextConfigLocation",value="application.properties")})
public class DispatchServlet extends HttpServlet {

    private static final String LOCATION = "contextConfigLocation";
    private static final String TEMPLATE_ROOT = "templateRoot";

    private static final List<HandlerMapping> handlerMappings = new ArrayList<>();
    private static final Map<HandlerMapping,HandlerAdapter> handlerAdaptes = new HashMap<>();
    private static final List<ViewResolver> viewResolvers = new ArrayList<>();


    @Override
    public void init(ServletConfig config) {
        try {
        ApplicationContext context = new ApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
        }catch (Exception e){
            System.out.println("init error");
            e.printStackTrace();
        }
    }



    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        try {
            doDispatch(req, resp);
        }catch (Exception e){
            resp.getOutputStream().print("500 error");
            e.printStackTrace();
        }

    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        HandlerMapping handler = getHandler(req);
        if(handler == null){
            resp.getOutputStream().print("404 not fount");
            return;
        }

        HandlerAdapter ha = getHandlerAdapter(handler);
        ModelAndView mv = ha.handle(req, resp ,handler);
        processDispatchResult(req, resp, handler, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler, ModelAndView mv) throws IOException {
        if(mv == null){
            return;
        }
        if(viewResolvers.isEmpty()){
            return;
        }

        for (ViewResolver viewResolver : viewResolvers) {
            if(!mv.getViewName().equals(viewResolver.getViewName())){
                continue;
            }
            String s = viewResolver.viewResolver(mv);

            resp.getOutputStream().print(s);
        }



    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if(handlerAdaptes.isEmpty()){
            return null;
        }
        HandlerAdapter handlerAdapter = handlerAdaptes.get(handler);
        return handlerAdapter;
    }

    private HandlerMapping getHandler(HttpServletRequest req) {

        if(handlerMappings.isEmpty()){
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        String finalUrl = url;
        HandlerMapping handler= handlerMappings.stream().filter(h -> h.getPattern().matcher(finalUrl).matches()).findAny().orElse(null);
        if(handler == null){
            return null;
        }
        return handler;
    }


    private void initStrategies(ApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);

    }

    private void initFlashMapManager(ApplicationContext context) {}

    /**
     * 实现动态模板的解析 （解析逻辑视图到具体视图实现）
     * @param context
     */
    private void initViewResolvers(ApplicationContext context)  {

        String templateRoot = context.getBeanDifinitionReader().getConfig().getProperty(TEMPLATE_ROOT);

        URL resource = this.getClass().getClassLoader().getResource(templateRoot);
        File file = new File(resource.getFile());

        List<File> list = Stream.of(file.listFiles()).flatMap(f -> f.isFile() ? Stream.of(f) : Stream.of(f.listFiles())).collect(Collectors.toList());

        list.forEach(l->viewResolvers.add(new ViewResolver(l.getName(),l)));
    }
    private void initRequestToViewNameTranslator(ApplicationContext context) {}
    private void initHandlerExceptionResolvers(ApplicationContext context) {}

    /**
     * 动态匹配method的参数，类型转换，参数赋值 （多类型的参数动态匹配）
     * @param context
     */
    private void initHandlerAdapters(ApplicationContext context) {

        handlerMappings.forEach(h->{
            Annotation[][] parameterAnnotations = h.getMethod().getParameterAnnotations();
            Map<String, Integer> paramterMap = new HashMap<>(16);
            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] parameterAnnotation = parameterAnnotations[i];
                for (Annotation annotation : parameterAnnotation) {
                    if(!(annotation instanceof RequestParameter)) {
                        continue;
                    }
                    String value = ((RequestParameter) annotation).value();
                    if("".equals(value.trim())){
                        value = value.getClass().getTypeName();
                    }
                    paramterMap.put(value,i);
                }
            }

            Class<?>[] parameterTypes = h.getMethod().getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if(parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class){
                    paramterMap.put(parameterType.getName(),i);
                }
            }

            HandlerAdapter handlerAdapter = new HandlerAdapter(  paramterMap );
            handlerAdaptes.put(h,handlerAdapter);

        });





    }

    /**
     * 用来保存Controller 中RequestMapping与method的对应关系 （将请求映射到处理器）
     * @param context
     */
    private void initHandlerMappings(ApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object instance = context.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if(!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }
            String baseUrl = "";
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                baseUrl = annotation.value();
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if(!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                String regex = baseUrl+ annotation.value().replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                HandlerMapping handlerMapping = new HandlerMapping(instance, method, pattern);
                handlerMappings.add(handlerMapping);
                System.out.println("mapping:" + regex + "," + method);
            }

        }


    }

    private void initThemeResolver(ApplicationContext context) {}
    private void initLocaleResolver(ApplicationContext context) {}
    private void initMultipartResolver(ApplicationContext context) {}




}
