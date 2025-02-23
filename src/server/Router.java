package server;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Router {
    private final Map<String, Map<String, BiFunction<String, String, String>>> routes;
    private final StaticFileHandler staticFileHandler;

    public Router() {
        this.routes = new HashMap<>();
        this.staticFileHandler = new StaticFileHandler();
    }

    public void get(String path, BiFunction<String, String, String> handler) {
        routes.computeIfAbsent("GET", k -> new HashMap<>()).put(path, handler);
    }

    public void post(String path, BiFunction<String, String, String> handler) {
        routes.computeIfAbsent("POST", k -> new HashMap<>()).put(path, handler);
    }

    public String handleRequest(String method, String path) {
        Map<String, BiFunction<String, String, String>> methodRoutes = routes.get(method);
        if (methodRoutes != null && methodRoutes.containsKey(path)) {
            return methodRoutes.get(path).apply(method, path);
        }
        return staticFileHandler.handle(path);
    }
}