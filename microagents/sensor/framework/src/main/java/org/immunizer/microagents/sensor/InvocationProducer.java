package org.immunizer.microagents.sensor;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class InvocationProducer {

    private static InvocationProducer singleton;
    private KafkaProducer<String, Invocation> producer;
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String BASE_TOPIC = "INV";
    private String topic;

    private InvocationProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.microagents.sensor.InvocationSerializer");
        producer = new KafkaProducer<String, Invocation>(props);
        topic = BASE_TOPIC + '/' + System.getProperty("swid") + '_' + System.getProperty("cxid");
    }

    public static InvocationProducer getSingleton() {
        if (singleton == null) {
            singleton = new InvocationProducer();
        }
        return singleton;
    }

    public void send(Invocation invocation) {
        try{
            System.out.print("XXXXXXXXXXXXXXXXX PRODUCER XXXXXXXXXXXXXXXX");
            System.out.println("Topic: " + topic);
            System.out.println("Invocation: " + invocation);
            producer.send(new ProducerRecord<String, Invocation>(topic, invocation.getCallStackId(), invocation));
            System.out.print("XXXXXXXXXXXXXXXXX PRODUCER XXXXXXXXXXXXXXXX");
        } catch(Throwable th) {
        }
    }
}