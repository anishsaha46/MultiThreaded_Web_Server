package server;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, Map<String, Object>> tables;
    
    public Database() {
        this.tables = new HashMap<>();
    }
}
