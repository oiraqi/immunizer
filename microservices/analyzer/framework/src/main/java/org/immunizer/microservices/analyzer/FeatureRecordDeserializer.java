package org.immunizer.microservices.analyzer;

import org.apache.kafka.common.serialization.Deserializer;
import com.google.gson.Gson;

public class FeatureRecordDeserializer implements Deserializer<FeatureRecord> {

    private Gson gson = new Gson();

    public FeatureRecordDeserializer() {
    }

    @Override
    public FeatureRecord deserialize(String topic, byte[] bytes) {
        System.out.println(new String(bytes));
        return gson.fromJson(new String(bytes), FeatureRecord.class);
    }
}