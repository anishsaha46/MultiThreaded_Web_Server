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

            // Security check for requuest size
            if(requestBuilder.length() > MAX_REQUEST_SIZE || contentLength > MAX_REQUEST_SIZE){
                sendError(out,413,"Request Too Large");
                return;
            }

            String request = requestBuilder.toString();
            if(!request.isEmpty()){
                String[] requestLines = request.split("\r\n");
                String[] requestLine = requestLines[0].split(" ");
                String method = requestLine[0];
                String path = requestLine[1];

                logger.info("Request: " + method + " " + path);

                // Basic Security Headers
                String response = router.handleRequest(method, path);
                out.print("HTTP/1.1 " + response.split("\r\n")[0] + "\r\n");
                out.print("X-Content-Type-Options: nosniff\r\n");
                out.print("X-Frame-Options: DENY\r\n");
                out.print("X-XSS-Protection: 1; mode=block\r\n");
                out.print(response.substring(response.indexOf("\r\n\r\n") + 4));
                out.flush();

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
