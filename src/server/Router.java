package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Router {
    private final Map<String, Map<String, BiFunction<String, String, String>>> routes;
    private final StaticFileHandler staticFileHandler;

    public Router() throws IOException {
        this.routes = new HashMap<>();
        this.staticFileHandler = new StaticFileHandler(new Config("config.properties").getStaticDir());
    }

    public void get(String path, BiFunction<String, String, String> handler) {
        routes.computeIfAbsent("GET", k -> new HashMap<>()).put(path, handler);
    }

    public void post(String path, BiFunction<String, String, String> handler) {
        routes.computeIfAbsent("POST", k -> new HashMap<>()).put(path, handler);
    }

    public String handleRequest(String method, String path, String request) throws IOException {
        Map<String, BiFunction<String, String, String>> methodRoutes = routes.get(method);
        if (methodRoutes != null && methodRoutes.containsKey(path)) {
            return methodRoutes.get(path).apply(request, path);
        }
        
        // Handle static files
        return staticFileHandler.handle(path);
    }
}