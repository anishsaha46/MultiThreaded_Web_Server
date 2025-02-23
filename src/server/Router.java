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
}
