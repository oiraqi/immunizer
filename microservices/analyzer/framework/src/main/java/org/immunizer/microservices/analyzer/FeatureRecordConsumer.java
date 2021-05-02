package org.immunizer.microservices.analyzer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaPairRDD;

import java.util.Collections;
import java.util.Properties;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;

import scala.Tuple2;

public class FeatureRecordConsumer {

    private Consumer<String, FeatureRecord> consumer;
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "Analyzer";
    private static final String TOPIC_PATTERN = "FRC/.+";
    private DistributedCache cache;
    private JavaSparkContext sc;

    public FeatureRecordConsumer(JavaSparkContext sc, DistributedCache cache) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.setProperty("group.id", GROUP_ID);
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.immunizer.microservices.analyzer.FeatureRecordDeserializer");

        consumer = new KafkaConsumer<String, FeatureRecord>(props);
        consumer.subscribe(Pattern.compile(TOPIC_PATTERN));
        this.cache = cache;
        this.sc = sc;
    }

    public Iterator<String> pollAndGetContexts(int timeout, int minBatchSize, int maxBatchSize) {
        ConsumerRecords<String, FeatureRecord> records = consumer.poll(Duration.ofSeconds(timeout));
        Vector<String> contexts = new Vector<String>();

        for (TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<String, FeatureRecord>> partitionRecords = records.records(partition);
            if (partitionRecords.size() < minBatchSize)
                continue;

            JavaPairRDD<Long, FeatureRecord> featureRecordsRDD = sc.parallelize(partitionRecords)
                    .mapToPair(record -> new Tuple2<Long, FeatureRecord>(record.offset(), record.value()));

            String context = partitionRecords.get(0).value().getSwid() + '/'
                    + partitionRecords.get(0).value().getCallStackId();

            cache.save(context, featureRecordsRDD);

            long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));

            if (lastOffset >= maxBatchSize) {
                contexts.add(context);
                cache.purge(context, lastOffset - maxBatchSize);
            }
        }
        return contexts.iterator();
    }

    public void close() {
        consumer.close();
    }
}