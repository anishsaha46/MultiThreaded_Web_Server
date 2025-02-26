package server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class TemplateRenderer {
    private final String templatesDir;

    public TemplateRenderer(String templatesDir){
        this.templatesDir = templatesDir;
    }

    public String render(String templateName,Map<String,Object> data){
        String template = loadTemplate(templateName);
        if(template == null) return "Template not found";
        String result = template;

        for(Map.Entry<String,Object>entry:data.entrySet()){
            result = result.replace("${" + entry.getKey() + "}", entry.getValue().toString());
        }
        return result;
    }
}
