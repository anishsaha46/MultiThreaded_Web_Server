package server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final Logger logger;
    private final Config config;
    private static final int MAX_REQUEST_SIZE = 8192;
    
}
