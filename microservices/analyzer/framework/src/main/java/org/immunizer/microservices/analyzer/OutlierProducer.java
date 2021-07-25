package org.immunizer.microservices.analyzer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;

public class OutlierProducer implements Serializable {

    private static final long serialVersionUID = 18764376L;

    private transient KafkaProducer<String, FeatureRecord> producer;
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";
    private static final String BASE_TOPIC = "OTL/";

    public OutlierProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.microservices.analyzer.FeatureRecordSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void send(FeatureRecord featureRecord) {
        producer.send(new ProducerRecord<>(BASE_TOPIC + featureRecord.getSwId() + '_' + featureRecord.getIcxId()
                + '_' + featureRecord.getCallStackId(), 0, "0", featureRecord));
    }

    public void close() {
        producer.close();
    }
}