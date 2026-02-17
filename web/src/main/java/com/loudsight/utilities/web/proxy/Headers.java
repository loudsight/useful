package com.loudsight.utilities.web.proxy;

import java.util.HashMap;
import java.util.Map;

public class Headers {
    private final Map<String, String> headers = new HashMap<>();

    public void add(String name, String value){
        headers.put(name, value);
    }

    public Map<String, String> getAll(){
        return headers;
    }
}
