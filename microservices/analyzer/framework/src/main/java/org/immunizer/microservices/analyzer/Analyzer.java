package org.immunizer.microservices.analyzer;

import static org.apache.spark.sql.functions.desc;

import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.outlier.LOF;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.api.java.JavaDoubleRDD;
import scala.Tuple2;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Analyzer {

    private static final String SPARK_MASTER_URL = "spark://spark-master:7077";
    private static final String APP_NAME = "Analyzer";
    private static final int POLL_PERIOD = 60;
    private static final int MIN_POLL_SIZE = 1000;
    private static final int MAX_BATCH_SIZE = 100000;
    private static final int MIN_POINTS = 400;
    private static final double OUTLIER_PROBABILITY_THRESHOLD = 0.98;

    public static void main(String[] args) throws Exception {
        SparkSession sparkSession = SparkSession.builder().appName(APP_NAME).master(SPARK_MASTER_URL).getOrCreate();
        JavaSparkContext sc = new JavaSparkContext(sparkSession.sparkContext());
        DistributedCache cache = new DistributedCache(sparkSession);
        FeatureRecordConsumer featureRecordConsumer = new FeatureRecordConsumer(sc, cache);
        OutlierProducer outlierProducer = new OutlierProducer();

        try {
            while (true) {
                Iterator<String> contexts = featureRecordConsumer.pollAndGetContexts(POLL_PERIOD, MIN_POLL_SIZE,
                        MAX_BATCH_SIZE);

                while (contexts.hasNext()) {
                    String context = contexts.next();
                    Dataset<Row> df = cache.fetch(context);

                    Dataset<Row> lofDS = new LOF().setMinPts(MIN_POINTS).transform(df);
                    JavaDoubleRDD lofRDD = lofDS.select("lof").toJavaRDD().<Double>map(row -> row.getDouble(0))
                            .mapToDouble(x -> x);
                    NormalDistribution nd = new NormalDistribution(lofRDD.mean(), lofRDD.stdev());
                    List<Long> outliers = lofDS.toJavaRDD()
                            .mapToPair(row -> new Tuple2<Long, Double>(row.getLong(0),
                                    2 * nd.cumulativeProbability(row.getDouble(1)) - 1))
                            .filter(record -> record._2 > OUTLIER_PROBABILITY_THRESHOLD).map(record -> record._1)
                            .collect();

                    outliers.forEach(key -> {
                        FeatureRecord fr = cache.get(context, key);
                        outlierProducer.send(fr);

                        // Get rid of these outliers for the next cycle
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