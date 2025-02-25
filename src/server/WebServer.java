package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WebServer {
    private final int port;
    private final Router router;
    private final Middleware middleware;
    private final SessionManager sessionManager;
    private final Database database;
    private volatile boolean running;
    private final Logger logger;
    private final Config config;
    private final ExecutorService threadPool;

    public WebServer(String configFile) throws Exception {
        this.config = new Config(configFile);
        this.port = config.getPort();
        this.router = new Router();
        this.middleware = new Middleware();
        this.sessionManager = new SessionManager();
        this.database = new Database();
        this.running = false;
        this.logger = Logger.getLogger("WebServer");
        FileHandler fh = new FileHandler(config.getLogFile());
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);

        // Initialize thread pool with a fixed number of threads
        int numberOfThreads = Runtime.getRuntime().availableProcessors() * 2;
        this.threadPool = Executors.newFixedThreadPool(numberOfThreads);

        // Register default routes
        router.get("/", (method, path) -> "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nWelcome!");

        // Register middleware
        middleware.use((request) -> {
            logger.info("Middleware: Logging request - " + request);
            return request; // Pass through the request
        });

        // Initialize database
        database.createTable("users");
    }

    public void start() throws IOException {
        running = true;
        logger.info("Starting server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New connection from " + clientSocket.getInetAddress());
                    // Use the thread pool to handle the request
                    threadPool.execute(new RequestHandler(clientSocket, router, middleware, sessionManager, database, logger, config));
                } catch (IOException e) {
                    logger.severe("Error accepting connection: " + e.getMessage());
                }
            }
        } finally {
            // Shutdown the thread pool when the server stops
            threadPool.shutdown();
        }
    }

    public void stop() {
        running = false;
    }

    public Router getRouter() {
        return router;
    }

    public Middleware getMiddleware() {
        return middleware;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Database getDatabase() {
        return database;
    }

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer("config.properties");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}