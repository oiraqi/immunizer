package org.immunizer.microagents.effector;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.Consumer;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.time.Duration;

public class AlarmConsumer {

    private Consumer<String, Alarm> consumer;
    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String GROUP_ID = "Effector";
    private static final String TOPIC = "Alarms";

    public AlarmConsumer () {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.setProperty("group.id", GROUP_ID);
        props.put("auto.offset.reset", "latest"); 
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.immunizer.microagents.effector.AlarmDeserializer");

        consumer = new KafkaConsumer<String, Alarm>(props);
        Collection<String> topics = Collections.singletonList(TOPIC + '/' + System.getProperty("swid"));        
        consumer.subscribe(topics);
        consumer.seekToBeginning(Collections.emptyList());
    }

    public ConsumerRecords<String, Alarm> poll (Duration timeout) {
        return consumer.poll(timeout);
    }

    public void close () {
        consumer.close();
    }
}