package com.zrs.spring.formework.springmvc;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public class ViewResolver {
    private String viewName;
    private File templateFile;

    private static Pattern PATTERN = Pattern.compile("@\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);

    public String viewResolver(ModelAndView mv) throws IOException {

        List<String> lines = Files.readAllLines(templateFile.toPath());
        StringBuffer sb = new StringBuffer();
        lines.forEach(s->{
            Matcher matcher =PATTERN.matcher(s);
            while (matcher.find()){
                for (int i = 0; i < matcher.groupCount() ; i++) {
                    String param = matcher.group(i);
                    String k = param.replaceAll("@\\{|\\}","");
                    Map<String, ?> model = mv.getModel();
                    Object o = model.get(k);
                    if(o ==null){
                        continue;
                    }
                    s = s.replace( param , o.toString());
                }
            }
            sb.append(s);
        });
        return sb.toString();
    }

}
