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


    public RequestHandler(Socket clientSocket, Router router, Logger logger, Config config) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void run(){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());

            BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream());

            clientSocket.setSoTimeout(5000);

            StringBuilder requestBuilder = new StringBuilder();

            String line;
            int contentLength = 0;

            // Read Header
            while((line = in.readLine()) != null && !line.isEmpty()){
                requestBuilder.append(line).append("\r\n");
                if(line.startsWith("Content-Length:")){
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
        } catch (IOException e) {
            logger.severe("Error handling request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.severe("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
