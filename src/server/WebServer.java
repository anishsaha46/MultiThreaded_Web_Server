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

        int numberofThreads = Runtime.getRuntime().availableProcessors() * 2;
        this.threadPool = Executors.newFixedThreadPool(numberofThreads);

        // Register default routes
        router.get("/", (method, path) -> "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\nWelcome!");
    }

    public void start() throws IOException {
        running = true;
        logger.info("Starting server on port" + port);
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while(running){
                try{
                    Socket clienSocket = serverSocket.accept();
                    logger.info("New Connection from"+clienSocket.getInetAddress())
                    threadPool.execute(new RequestHandler(clienSocket, router, logger,config));
                } catch(IOException e){
                    logger.severe("Error accepting connection"+e.getMessage());
                }
            } finally{
                threadPool.shutdown();
            }
        }
    }
}
