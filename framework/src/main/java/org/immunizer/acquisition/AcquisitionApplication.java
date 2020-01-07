package org.immunizer.acquisition;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.immunizer.acquisition.Invocation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

public class AcquisitionApplication {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "test");
        props.put("auto.offset.reset", "earliest"); 
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.immunizer.acquisition.consumer.InvocationDeserializer");

        Consumer<String, Invocation> consumer = new KafkaConsumer<String, Invocation>(props);
        Collection<String> topics = Collections.singletonList("Test");        
        consumer.subscribe(topics);
        consumer.seekToBeginning(Collections.emptyList());
        System.out.println("Hi!!!");
        FeatureExtractor featureExtractor = FeatureExtractor.getSingleton();

        try {            
            while (true) {
                System.out.println("Hi!");
                ConsumerRecords<String, Invocation> records = consumer.poll(Duration.ofSeconds(5));
                System.out.println(records.count());
                for (ConsumerRecord<String, Invocation> record : records){
                    System.out.printf("offset = %d, key = %s, value = %s%n",
                        record.offset(), record.key(), record.value());
                    FeatureRecord featureRecord = featureExtractor.extract(record.value());
                    if (featureRecord != null) {
                        featureExtractor.log(featureRecord);
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

}