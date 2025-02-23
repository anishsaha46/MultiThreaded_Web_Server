package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class StaticFileHandler {
    private final String staticDir;
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();

    static {
        CONTENT_TYPES.put(".html", "text/html");
        CONTENT_TYPES.put(".css", "text/css");
        CONTENT_TYPES.put(".js", "application/javascript");
        CONTENT_TYPES.put(".png", "image/png");
        CONTENT_TYPES.put(".jpg", "image/jpeg");
        CONTENT_TYPES.put(".jpeg", "image/jpeg");
        CONTENT_TYPES.put(".gif", "image/gif");
        CONTENT_TYPES.put(".svg", "image/svg+xml");
        CONTENT_TYPES.put(".txt", "text/plain");
        CONTENT_TYPES.put(".json", "application/json");
    }

    public StaticFileHandler(String staticDir) {
        this.staticDir = staticDir;
    }

    public String handle(String path) {
        try {
            // Normalize the path to prevent directory traversal attacks
            Path filePath = Paths.get(staticDir, path.equals("/") ? "index.html" : path).normalize();
            if (!filePath.startsWith(staticDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                return "HTTP/1.1 404 Not Found\r\nContent-Type: text/plain\r\n\r\n404 - File Not Found";
            }

            String extension = filePath.toString().substring(filePath.toString().lastIndexOf(".")).toLowerCase();
            String contentType = CONTENT_TYPES.getOrDefault(extension, "application/octet-stream");
            byte[] content = Files.readAllBytes(filePath);

            return String.format("HTTP/1.1 200 OK\r\nContent-Type: %s\r\nContent-Length: %d\r\n\r\n",
                contentType, content.length) + new String(content);
        } catch (IOException e) {
            return "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\n500 - Server Error";
        }
    }
}