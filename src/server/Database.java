package server;

import java.util.HashMap;
import java.util.Map;

public class Database {
    private final Map<String, Map<String, Object>> tables;
    
    public Database() {
        this.tables = new HashMap<>();
    }

    public void createTable(String tableName){
        tables.putIfAbsent(tableName,new HashMap<>());
    }

    public void insert(String tableName, String key, Object value){
        Map<String,Object> table = tables.get(tableName);
        if(table == null){
            throw new IllegalArgumentException("Table"+tableName+"Table does not exist");
        }
        table.put(key,value);
    }

    public Object get(String tableName, String key){
        Map<String,Object> table = tables.get(tableName);
        if(table == null){
            throw new IllegalArgumentException("Table"+tableName+"Table does not exist");
        }
        return table.get(key);
    }

    public void update(String tableName, String key, Object value){
        Map<String,Object> table = tables.get(tableName);
        if(table == null){
            throw new IllegalArgumentException("Table"+tableName+"Table does not exist");
        }
        if(!table.containsKey(key)){
            throw new IllegalArgumentException("Key "+key+" does not exist in table"+tableName);
        }
        table.put(key,value);
    }
    
    public void delete(String tableName, String key){
        Map<String,Object> table = tables.get(tableName);
        if(table == null){
            throw new IllegalArgumentException("Table"+tableName+"Table does not exist");
        }
        if(!table.containsKey(key)){
            throw new IllegalArgumentException("Key "+key+" does not exist in table"+tableName);
        }
        table.remove(key);
    }
}
