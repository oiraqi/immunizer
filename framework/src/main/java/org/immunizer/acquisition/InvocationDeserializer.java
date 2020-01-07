package org.immunizer.acquisition;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;
import org.immunizer.acquisition.Invocation;

public class InvocationDeserializer implements Deserializer<Invocation> {

    private Gson gson = new Gson();

    public InvocationDeserializer() {}
    
    @Override
    public Invocation deserialize(String topic, byte[] bytes) {
        System.out.println(new String(bytes));
        return gson.fromJson(new String(bytes), Invocation.class);
    }
}