package server;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final Middleware middleware;
    private final Logger logger;
    private final Config config;
    private static final int MAX_REQUEST_SIZE = 8192;

    public RequestHandler(Socket clientSocket, Router router, Middleware middleware, Logger logger, Config config) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.middleware = middleware;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
            BufferedOutputStream dataOut = new BufferedOutputStream(clientSocket.getOutputStream())
        ) {
            clientSocket.setSoTimeout(5000); // 5 second timeout
            
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            int contentLength = 0;
            
            // Read headers
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
                if (line.startsWith("Content-Length:")) {
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
            
            // Security check for request size
            if (requestBuilder.length() > MAX_REQUEST_SIZE || contentLength > MAX_REQUEST_SIZE) {
                sendError(out, 413, "Request Entity Too Large");
                return;
            }

            String request = requestBuilder.toString();
            if (!request.isEmpty()) {
                String[] requestLines = request.split("\r\n");
                String[] requestLine = requestLines[0].split(" ");
                String method = requestLine[0];
                String path = sanitizePath(requestLine[1]);
                
                logger.info(method + " " + path);
                
                // Process middleware
                String processedRequest = middleware.process(request);
                
                // Handle the request with the router
                String response = router.handleRequest(method, path, processedRequest);
                
                // Send response
                out.print("HTTP/1.1 " + response.split("\r\n")[0] + "\r\n");
                out.print("X-Content-Type-Options: nosniff\r\n");
                out.print("X-Frame-Options: DENY\r\n");
                out.print("X-XSS-Protection: 1; mode=block\r\n");
                out.print(response.substring(response.indexOf("\r\n\r\n") + 4));
                out.flush();
            }
        } catch (IOException e) {
            logger.severe("Request handling error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.severe("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private String sanitizePath(String path) {
        return path.replaceAll("\\.\\./", ""); // Prevent directory traversal
    }
    
    private void sendError(PrintWriter out, int code, String message) {
        out.print(String.format("HTTP/1.1 %d %s\r\nContent-Type: text/plain\r\n\r\n%s", code, message, message));
        out.flush();
    }
}