package org.immunizer.microservices.planner;

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
import java.util.StringTokenizer;

public class Planner {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka:9092";
    private static final String GROUP_ID = "Planner";
    private static final String TOPIC_PATTERN = "OTL/.+";
    private static final int BATCH_DURATION = 60;

    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("Monitor").setMaster("spark://spark-master:7077");
        JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, Durations.seconds(BATCH_DURATION));

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS);
        kafkaParams.put("group.id", GROUP_ID);
        kafkaParams.put("enable.auto.commit", "true");
        kafkaParams.put("auto.commit.interval.ms", "1000");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.immunizer.microservices.planner.FeatureRecordDeserializer");

        SuspectProducer suspectProducer = new SuspectProducer();

        JavaInputDStream<ConsumerRecord<String, FeatureRecord>> outlierStream = KafkaUtils.createDirectStream(jsc,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.SubscribePattern(Pattern.compile(TOPIC_PATTERN), kafkaParams));
        outlierStream.map(ConsumerRecord::value).map(outlier -> {
            var suspects = outlier.getSuspects().entrySet().iterator();
            while(suspects.hasNext()) {
                var suspect = suspects.next();
                var key = suspect.getKey();
                StringTokenizer strk = new StringTokenizer("_");
                var paramIndex = Integer.parseInt(strk.nextToken());
                var featureIndex = Integer.parseInt(strk.nextToken());
                
                if (featureIndex == 1 || featureIndex == 3 || featureIndex == 4) {
                    var path = suspect.getValue();
                    suspectProducer.send(new Suspect(outlier.getSwId(), outlier.getCallStackId(), outlier.getFullyQualifiedMethodName(),
                    paramIndex, featureIndex, path));
                } else if (featureIndex == 21 || featureIndex == 23) {
                    StringTokenizer strt = new StringTokenizer(suspect.getValue(), "/");
                    var path = strt.nextToken();
                    var chars = strt.nextToken();
                    suspectProducer.send(new Suspect(outlier.getSwId(), outlier.getCallStackId(), outlier.getFullyQualifiedMethodName(),
                    paramIndex, featureIndex, path, chars));
                } else if (featureIndex == 5) {
                    suspectProducer.send(new Suspect(outlier.getSwId(), outlier.getCallStackId(), outlier.getFullyQualifiedMethodName(),
                    paramIndex, 5));
                }
                
            }

            return null;
        });

        jsc.start();
        jsc.awaitTermination();
    }
}