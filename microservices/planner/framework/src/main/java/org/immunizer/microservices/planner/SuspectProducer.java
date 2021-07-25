package org.immunizer.microservices.planner;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.Serializable;

public class SuspectProducer implements Serializable {

    private static final long serialVersionUID = 1876876L;

    private transient KafkaProducer<String, Suspect> producer;
    private static final String BOOTSTRAP_SERVERS = "localhost:29092";
    private static final String BASE_TOPIC = "SPC/";

    public SuspectProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.microservices.planner.SuspectSerializer");
        producer = new KafkaProducer<>(props);
    }

    public void send(Suspect suspect) {
        producer.send(new ProducerRecord<>(BASE_TOPIC + suspect.getSwId(), 0, "0", suspect));
    }
}