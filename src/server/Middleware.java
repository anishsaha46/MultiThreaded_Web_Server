package server;

import java.util.ArrayList;
import java.util.List;

public class Middleware {
    private final List<MiddlewareHandlers> handlers;

    public Middleware() {
        this.handlers = new ArrayList<>();
    }

    public void use(MiddlewareHandler handler) {
        handlers.add(handler);
    }

    public String process(String request){
        String result = request;
        for (MiddlewareHandler handler : handlers) {
            result = handler.handle(result);
        }
        return result;
    }
}
