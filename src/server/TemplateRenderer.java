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
}
