package org.immunizer.acquisition.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.immunizer.acquisition.Invocation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class ConsumerApplication {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.immunizer.acquisition.consumer.InvocationDeserializer");

        Consumer<String, Invocation> consumer = new KafkaConsumer<String, Invocation>(props);
        Collection<String> topics = Collections.singletonList("Test1");
        consumer.subscribe(topics);

        while (true) {
            ConsumerRecords<String, Invocation> records = consumer.poll(Duration.ofSeconds(15));
            for (ConsumerRecord<String, Invocation> record : records)
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
        }
    }

}