package com.loudsight.utilities.web.proxy;

import java.util.HashMap;
import java.util.Map;

public class Headers {
    private final Map<String, String> values = new HashMap<>();

    public void add(String name, String value){
        values.put(name, value);
    }

    public Map<String, String> getAll(){
        return values;
    }
}
