package org.immunizer.acquisition.producer;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.immunizer.acquisition.Invocation;

public class Producer {

    private static Producer singleton;
    private KafkaProducer<String, Invocation> producer;

    private Producer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka-container:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.acquisition.producer.InvocationSerializer");
        producer = new KafkaProducer<String, Invocation>(props);
    }

    public static Producer getSingleton() {
        if (singleton == null) {
            singleton = new Producer();
        }
        return singleton;
    }

    public void send(Invocation invocation) {
        producer.send(new ProducerRecord<String, Invocation>("Test1", invocation));
    }
}