package org.immunizer.microservices.analyzer;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.Dataset;
import static org.apache.spark.sql.functions.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaPairRDD;

import java.util.List;
import java.util.Iterator;

import scala.Tuple2;

import org.apache.spark.ml.outlier.LOF;

public class Analyzer {

    private static final String SPARK_MASTER_URL = "spark://spark-master:7077";
    private static final String APP_NAME = "Analyzer";
    private static final int BATCH_DURATION = 60;
    private static final int MIN_BATCH_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 100000;
    private static final int MIN_POINTS = 1000;
    private static final int TOP_OUTLIERS = 10;

    public static void main(String[] args) throws Exception {
        SparkSession sparkSession = SparkSession.builder().appName(APP_NAME).master(SPARK_MASTER_URL).getOrCreate();
        JavaSparkContext sc = new JavaSparkContext(sparkSession.sparkContext());
        DistributedCache cache = new DistributedCache(sparkSession);
        FeatureRecordConsumer featureRecordConsumer = new FeatureRecordConsumer(sc, cache);
        OutlierProducer outlierProducer = new OutlierProducer();
        
        try {
            while(true) {
                Iterator<String> contexts = featureRecordConsumer.pollAndGetContexts(
                                                            BATCH_DURATION,
                                                            MIN_BATCH_SIZE, MAX_BATCH_SIZE);

                while(contexts.hasNext()) {
                    String context = contexts.next();
                    Dataset<Row> df = cache.fetch(context);                    
                    List<Row> outliers = new LOF().setMinPts(MIN_POINTS).transform(df)
                                                    .sort(desc("lof")).takeAsList(TOP_OUTLIERS);
                    outliers.forEach(outlier -> {
                        long key = (Long)outlier.get(0);
                        FeatureRecord fr = cache.get(context, key);
                        outlierProducer.send(fr);
                        cache.delete(context, key);
                    });
                }
            }
        } finally {
            featureRecordConsumer.close();
            outlierProducer.close();
        }
    }
}