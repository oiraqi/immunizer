package org.immunizer.microagents.effector;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;

public class AlarmDeserializer implements Deserializer<Alarm> {

    private Gson gson = new Gson();

    public AlarmDeserializer() {}
    
    @Override
    public Alarm deserialize(String topic, byte[] bytes) {
        return gson.fromJson(new String(bytes), Alarm.class);
    }
}