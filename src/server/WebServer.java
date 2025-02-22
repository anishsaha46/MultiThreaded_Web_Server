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
    private final ExecutorService thService
    
}
