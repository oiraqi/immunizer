package org.immunizer.microagents.sensor;

import org.apache.kafka.common.serialization.Serializer;
import com.google.gson.Gson;

public class InvocationSerializer implements Serializer<Invocation> {

    private Gson gson = new Gson();

    public InvocationSerializer() {}
    
    @Override
    public byte[] serialize(String topic, Invocation invocation) {
        return gson.toJson(invocation).getBytes();
    }
}