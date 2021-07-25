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
import org.apache.commons.math3.special.Erf;

public class Analyzer {

    private static final String SPARK_MASTER_URL = "spark://spark-master:7077";
    private static final String APP_NAME = "Analyzer";
    private static final int POLL_PERIOD = 60;
    private static final int MIN_POLL_SIZE = 5000;
    private static final int MIN_BATCH_SIZE = 50000;
    private static final int MAX_BATCH_SIZE = 100000;
    private static final int MIN_POINTS = 400;
    private static final double OUTLIER_RATIO = 1 / 10000;

    public static void main(String[] args) throws Exception {
        SparkSession sparkSession = SparkSession.builder().appName(APP_NAME).master(SPARK_MASTER_URL).getOrCreate();
        JavaSparkContext sc = new JavaSparkContext(sparkSession.sparkContext());
        DistributedCache cache = new DistributedCache(sparkSession);
        FeatureRecordConsumer featureRecordConsumer = new FeatureRecordConsumer(sc, cache);
        OutlierProducer outlierProducer = new OutlierProducer();
        double y = Erf.erfInv(1 - OUTLIER_RATIO * 2) * Math.sqrt(2);

        try {
            while (true) {
                Iterator<String> contexts = featureRecordConsumer.pollAndGetContexts(POLL_PERIOD, MIN_POLL_SIZE,
                        MIN_BATCH_SIZE, MAX_BATCH_SIZE);

                while (contexts.hasNext()) {
                    String context = contexts.next();
                    Dataset<Row> df = cache.fetch(context);

                    Dataset<Row> lofDS = new LOF().setMinPts(MIN_POINTS).transform(df);
                    JavaDoubleRDD lofRDD = lofDS.select("lof").toJavaRDD().<Double>map(row -> row.getDouble(0))
                            .mapToDouble(x -> x);
                    double mean = lofRDD.mean();
                    double sd = lofRDD.stdev();
                    double threshold = mean + y * sd;
                    
                    // Pick only those records whose LOF deviates from the mean
                    // by more than y * sd, i.e., only top OUTLIER_PERCENTAGE,
                    // as y has been computed on this basis (using erfInv)
                    List<Long> outliers = lofDS.toJavaRDD()
                            .filter(record -> record.getDouble(1) > threshold)
                            .map(record -> record.getLong(0))
                            .collect();

                    outliers.forEach(key -> {
                        FeatureRecord fr = cache.get(context, key);
                        outlierProducer.send(fr);

                        // Get rid of these outliers for a cleaner next cycle
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