package server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import server.HttpResponse;
import server.Config;
import server.StaticFileHandler;
import server.HttpRequest;

public class Router {
    private final Map<String, Map<String, BiFunction<HttpRequest, String, HttpResponse>>> routes;
    private final StaticFileHandler staticFileHandler;

    public Router(Config config) throws IOException {
        this.routes = new HashMap<>();
        this.staticFileHandler = new StaticFileHandler(config.getStaticDir());
    }

    public void get(String path, BiFunction<HttpRequest, String, HttpResponse> handler) {
        routes.computeIfAbsent("GET", k -> new HashMap<>()).put(path, handler);
    }

    public void post(String path, BiFunction<HttpRequest, String, HttpResponse> handler) {
        routes.computeIfAbsent("POST", k -> new HashMap<>()).put(path, handler);
    }

    public HttpResponse handleRequest(HttpRequest request) throws IOException {
        Map<String, BiFunction<HttpRequest, String, HttpResponse>> methodRoutes = routes.get(request.getMethod());
        if (methodRoutes != null && methodRoutes.containsKey(request.getPath())) {
            return methodRoutes.get(request.getPath()).apply(request, request.getPath());
        }
        
        // Handle static files
        return staticFileHandler.handle(request.getPath());
    }
}