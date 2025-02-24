package server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private final Map<String,Map<String , Object>> sessions;
    
    public SessionManager() {
        this.sessions = new HashMap<>();
    }
    
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new HashMap<>());
        return sessionId;
    }
}
