package org.immunizer.instrumentation;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.immunizer.instrumentation.Invocation;

public class InvocationProducer {

    private static InvocationProducer singleton;
    private KafkaProducer<String, Invocation> producer;

    private InvocationProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka-container:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.acquisition.producer.InvocationSerializer");
        producer = new KafkaProducer<String, Invocation>(props);
    }

    public static InvocationProducer getSingleton() {
        if (singleton == null) {
            singleton = new InvocationProducer();
        }
        return singleton;
    }

    public void send(Invocation invocation) {
        producer.send(new ProducerRecord<String, Invocation>("Invocations", invocation));
    }
}