package server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Router {
    private final Map<String,Map<String,BiFunction<String,String,String>>> routes;

    private final StaticFileHandler staticFileHandler;

    public Router() {
        this.routes = new HashMap<>();
        this.staticFileHandler = new StaticFileHandler();
    }

    public void get(String path,BiFunction<String,String,String> handler){
        routes.computeIfAbsent("GET",k -> new HashMap<>()).put(path,handler);
    }
}
