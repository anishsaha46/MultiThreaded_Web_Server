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
}
