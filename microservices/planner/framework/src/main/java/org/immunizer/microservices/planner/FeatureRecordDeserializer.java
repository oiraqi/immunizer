package org.immunizer.microservices.planner;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;

public class FeatureRecordDeserializer implements Deserializer<FeatureRecord> {

    private Gson gson = new Gson();

    @Override
    public FeatureRecord deserialize(String topic, byte[] bytes) {
        System.out.println(new String(bytes));
        return gson.fromJson(new String(bytes), FeatureRecord.class);
    }
}