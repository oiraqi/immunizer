package org.immunizer.microservices.analyzer;

import org.apache.kafka.common.serialization.Serializer;
import com.google.gson.Gson;
import java.io.Serializable;

public class FeatureRecordSerializer implements Serializer<FeatureRecord>, Serializable {

    private static final long serialVersionUID = 1274353L;

    private Gson gson = new Gson();

    public FeatureRecordSerializer() {}
    
    @Override
    public byte[] serialize(String topic, FeatureRecord featureRecord) {
        return gson.toJson(featureRecord).getBytes();
    }
}