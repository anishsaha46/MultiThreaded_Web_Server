package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;
import server.HttpRequest;
import server.HttpResponse;
import java.util.Map;

public class RequestHandler implements Runnable {
    private final Socket clientSocket;
    private final Router router;
    private final Middleware middleware;
    @SuppressWarnings("unused")
    private final SessionManager sessionManager;
    @SuppressWarnings("unused")
    private final Database database;
    @SuppressWarnings("unused")
    private final TemplateRenderer templateRenderer;
    private final Logger logger;
    @SuppressWarnings("unused")
    private final Config config;
    private static final int MAX_REQUEST_SIZE = 8192; // Max header size
    private static final int MAX_BODY_SIZE = 1024 * 1024; // Max body size (1MB)
    private static final int SOCKET_TIMEOUT_MS = 5000; // 5 seconds

    public RequestHandler(Socket clientSocket, Router router, Middleware middleware, SessionManager sessionManager, Database database, TemplateRenderer templateRenderer, Logger logger, Config config) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.middleware = middleware;
        this.sessionManager = sessionManager;
        this.database = database;
        this.templateRenderer = templateRenderer;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void run() {
        HttpRequest request = null;
        HttpResponse response = null;
        // Declare streams outside try-with-resources to access in catch blocks
        InputStream inputStream = null; 
        OutputStream outputStream = null; 

        try {
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();
            clientSocket.setSoTimeout(SOCKET_TIMEOUT_MS);

            request = parseRequest(inputStream);
            if (request == null) {
                // Error response already sent by parseRequest
                return; 
            }
            
            logger.info(request.getMethod() + " " + request.getPath());

            // TODO: Enhance middleware to work with HttpRequest/HttpResponse
            // String processedRequestHeaders = middleware.process(request.getRawHeaders()); 
            
            // Call router with the HttpRequest object
            response = router.handleRequest(request);

            // Send successful response using the original outputStream
            sendResponse(outputStream, response); 

        } catch (SocketTimeoutException e) {
            logger.warning("Socket timeout: " + e.getMessage());
            response = createErrorResponse(408, "Request Timeout");
            // Attempt to send error response if outputStream is available
            if (outputStream != null) {
                try { sendResponse(outputStream, response); } catch (IOException ioe) { logger.severe("Error sending timeout response: " + ioe.getMessage()); }
            }
        } catch (IOException e) {
            logger.severe("Request handling I/O error: " + e.getMessage());
            response = createErrorResponse(500, "Internal Server Error");
            // Attempt to send error response
            if (outputStream != null) {
                 try { sendResponse(outputStream, response); } catch (IOException ioe) { logger.severe("Error sending IO error response: " + ioe.getMessage()); }
            }
        } catch (Exception e) { // Catch unexpected errors during processing
            logger.log(java.util.logging.Level.SEVERE, "Unexpected error processing request", e);
            response = createErrorResponse(500, "Internal Server Error");
            // Attempt to send error response
            if (outputStream != null) {
                 try { sendResponse(outputStream, response); } catch (IOException ioe) { logger.severe("Error sending generic error response: " + ioe.getMessage()); }
            }
        } finally {
            // Ensure streams are closed (redundant if socket closes, but good practice)
            try { if (inputStream != null) inputStream.close(); } catch (IOException e) { /* ignore */ }
            try { if (outputStream != null) outputStream.close(); } catch (IOException e) { /* ignore */ }
            // Finally, ensure the socket is always closed
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                logger.severe("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private HttpRequest parseRequest(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        HttpRequest request = new HttpRequest();
        
        // Read request line
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            sendErrorResponse(clientSocket.getOutputStream(), 400, "Bad Request"); // Send error directly as parsing failed early
            return null; 
        }
        
        String[] requestLineParts = requestLine.split(" ");
        if (requestLineParts.length != 3) {
             sendErrorResponse(clientSocket.getOutputStream(), 400, "Bad Request");
            return null;
        }
        request.setMethod(requestLineParts[0]);
        request.setPath(sanitizePath(requestLineParts[1]));
        request.setHttpVersion(requestLineParts[2]);
        
        // Read headers
        String headerLine;
        int headersLength = 0;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            headersLength += headerLine.length();
            if (headersLength > MAX_REQUEST_SIZE) {
                sendErrorResponse(clientSocket.getOutputStream(), 413, "Headers Too Large");
                return null;
            }
            String[] headerParts = headerLine.split(":", 2);
            if (headerParts.length == 2) {
                request.addHeader(headerParts[0].trim(), headerParts[1].trim());
            }
        }
        
        // Read body if Content-Length is present
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthHeader);
                if (contentLength > MAX_BODY_SIZE) {
                    sendErrorResponse(clientSocket.getOutputStream(), 413, "Payload Too Large");
                    return null;
                }
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int bytesRead = reader.read(bodyChars, 0, contentLength);
                    if (bytesRead != contentLength) {
                         sendErrorResponse(clientSocket.getOutputStream(), 400, "Bad Request - Incomplete Body");
                         return null;
                    }
                    // Assuming body is text for now. For binary, read bytes directly from InputStream
                    request.setBody(new String(bodyChars).getBytes()); 
                }
            } catch (NumberFormatException e) {
                 sendErrorResponse(clientSocket.getOutputStream(), 400, "Bad Request - Invalid Content-Length");
                 return null;
            }
        }
        
        return request;
    }

    private void sendResponse(OutputStream outputStream, HttpResponse response) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        
        // Status Line
        writer.write(String.format("HTTP/1.1 %d %s\r\n", response.getStatusCode(), response.getStatusMessage()));
        
        // Headers
        if (response.getHeaders() != null) {
            for (Map.Entry<String, String> header : response.getHeaders().entrySet()) {
                writer.write(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
            }
        }
        writer.write("Connection: close\r\n"); // Ensure connection closes
        writer.write("\r\n");
        writer.flush();
        
        // Body
        if (response.getBody() != null && response.getBody().length > 0) {
            outputStream.write(response.getBody());
            outputStream.flush();
        }
    }
    
    // Helper to create a simple error response object
    private HttpResponse createErrorResponse(int code, String message) {
        HttpResponse response = new HttpResponse();
        response.setStatusCode(code);
        response.setStatusMessage(message);
        response.addHeader("Content-Type", "text/plain");
        response.setBody(String.format("%d - %s", code, message).getBytes());
        return response;
    }

    // Helper to send immediate error during parsing (before full response object is ready)
    private void sendErrorResponse(OutputStream outputStream, int code, String message) throws IOException {
         HttpResponse response = createErrorResponse(code, message);
         sendResponse(outputStream, response);
    }

    private String sanitizePath(String path) {
        // Basic sanitization, consider a more robust library for production
        if (path == null) return "/";
        path = path.replaceAll("%20", " "); // Decode spaces
        path = path.replaceAll("\\.\\./", ""); // Prevent basic traversal
        if (path.contains("..") || path.contains(":")) { // Fixed check
             return "/"; // Block more complex traversal or drive access
        }
        return path;
    }
}