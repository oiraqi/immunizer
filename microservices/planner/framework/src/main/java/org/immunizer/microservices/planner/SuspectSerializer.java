package org.immunizer.microservices.planner;

import org.apache.kafka.common.serialization.Serializer;
import com.google.gson.Gson;
import java.io.Serializable;

public class SuspectSerializer implements Serializer<Suspect>, Serializable {

    private static final long serialVersionUID = 1274353L;

    private transient Gson gson = new Gson();
    
    @Override
    public byte[] serialize(String topic, Suspect suspect) {
        return gson.toJson(suspect).getBytes();
    }
}