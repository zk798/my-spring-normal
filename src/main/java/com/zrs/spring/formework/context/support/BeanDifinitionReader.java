package com.zrs.spring.formework.context.support;

import com.zrs.spring.formework.beans.BeanDifinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

/**
 * 对配置文件加载查找解析
 */
public class BeanDifinitionReader {

    private static final String SCAN_PACKAGE="scanPackage";
    private Properties config = new Properties();

    private List<String> registyBeanClasses = new ArrayList<>();

    public BeanDifinitionReader(String... locations){
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(locations[0]);
        try {
            config.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(resourceAsStream != null){
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    public BeanDifinition registerBean(String className){
        if(!registyBeanClasses.contains(className)){
            return null;
        }
        BeanDifinition beanDifinition = new BeanDifinition();
        beanDifinition.setBeanClassName(className);
        beanDifinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));
        beanDifinition.setLazyInit(false);
        return beanDifinition;
    }

    public Properties getConfig(){
        return config;
    }

    public List<String> loadBeanDefinitions(){
        return this.registyBeanClasses;
    }

    /**
     * 扫描的包
     * @param packageName 包名
     */
    private void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource( packageName.replaceAll("\\.", "/"));
        try {
            DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(url.toURI()));
            for (Path path : paths) {
                if(Files.isDirectory(path)){
                    doScanner(packageName+"."+path.getFileName());
                }else{
                    registyBeanClasses.add(packageName+"."+path.getFileName().toString().replace(".class",""));
                }
            }

        }  catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private String lowerFirstCase(String str){
        char[] chars = str.toCharArray();
        chars[0] +=32;
        return String.valueOf(chars);

    }


}
