package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private final Properties properties;
    
    public Config(String configFile) throws IOException {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        }
    }
    
    public int getPort() {
        return Integer.parseInt(properties.getProperty("port", "8080"));
    }
    
    public String getStaticDir() {
        return properties.getProperty("static.dir", "static");
    }
    
    public String getLogFile() {
        return properties.getProperty("log.file", "server.log");
    }
}