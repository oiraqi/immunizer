package org.immunizer.acquisition.sensor;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.immunizer.acquisition.Invocation;

public class KafkaSensor extends Sensor {

    private Producer<String, Invocation> producer;

    public KafkaSensor() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka-container:9092");
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.immunizer.acquisition.sensor.KafkaInvocationSerializer");
        producer = new KafkaProducer<String, Invocation>(props);
    }

    public void send(Invocation invocation) {
        producer.send(new ProducerRecord<String, Invocation>("Test", invocation));
        System.out.println("++++++++++ KAFKA " + invocation.getNumberOfParams() + " KAFKA ++++++++++");
    }
}