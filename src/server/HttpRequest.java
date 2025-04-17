package server;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private String httpVersion;
    private Map<String, String> headers;
    private byte[] body;

    public HttpRequest() {
        this.headers = new HashMap<>();
        this.body = new byte[0]; // Default to empty body
    }

    // Getters
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public String getHttpVersion() { return httpVersion; }
    public Map<String, String> getHeaders() { return headers; }
    public byte[] getBody() { return body; }
    public String getBodyAsString() { return new String(body); } // Convenience method

    // Setters (typically used during parsing)
    public void setMethod(String method) { this.method = method; }
    public void setPath(String path) { this.path = path; }
    public void setHttpVersion(String httpVersion) { this.httpVersion = httpVersion; }
    public void addHeader(String key, String value) { this.headers.put(key.toLowerCase(), value); } // Store headers lower-case
    public void setBody(byte[] body) { this.body = body; }

    public String getHeader(String key) {
        return this.headers.get(key.toLowerCase());
    }
}
