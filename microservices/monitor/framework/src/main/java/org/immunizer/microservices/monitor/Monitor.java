package org.immunizer.microservices.monitor;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Monitor {

    private static final String BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String GROUP_ID = "Monitor";
    private static final String TOPIC_PATTERN = "Invocations/.+";
    private static final int BATCH_DURATION = 60;

    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("Monitor").setMaster("spark://spark-master:7077");
        JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(BATCH_DURATION));
        DistributedCache cache = new DistributedCache(jsc.sparkContext());

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        kafkaParams.put("group.id", GROUP_ID);
        kafkaParams.put("enable.auto.commit", "true");
        kafkaParams.put("auto.commit.interval.ms", "1000");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");

        JavaInputDStream<ConsumerRecord<String, byte[]>> invocationStream = KafkaUtils.createDirectStream(jsc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.SubscribePattern(Pattern.compile(TOPIC_PATTERN), kafkaParams));
        JavaDStream<String> modelStream = invocationStream.map(ConsumerRecord::value).flatMap(new ModelMapper())
                .filter(record -> record != null);
        modelStream.foreachRDD(model -> cache.updateModel(model));

        jsc.start();
        jsc.awaitTermination();
    }
}