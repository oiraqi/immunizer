package org.immunizer.microagents.sensor;

import java.util.Map;
import java.util.HashMap;

public class Context {

    private static ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>() {
        @Override
        protected Map<String, String> initialValue() {
            return new HashMap<String, String>();
        }
    };

    public static String get(String key) {
        return threadLocal.get().get(key);
    }

    public static void set(String key, String value) {
        threadLocal.get().put(key, value);
    }
    
}
