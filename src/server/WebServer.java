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
    private volatile boolean running;
    private final Logger logger;
    private final Config config;
    private final ExecutorService threadPool;

    public WebServer(String configFile) throws Exception {
        this.config = new Config(configFile);
        this.port = config.getPort();
        this.router = new Router();
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
                    threadPool.execute(new RequestHandler(clientSocket, router, logger, config));
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

    public static void main(String[] args) {
        try {
            WebServer server = new WebServer("config.properties");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}