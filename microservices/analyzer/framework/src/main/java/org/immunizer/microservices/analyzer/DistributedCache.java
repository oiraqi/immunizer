package org.immunizer.microservices.analyzer;

import org.apache.ignite.spark.JavaIgniteContext;
import org.apache.ignite.spark.JavaIgniteRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.ml.linalg.VectorUDT;

public class DistributedCache {

    private JavaIgniteContext<Long, FeatureRecord> igniteContext;
    private SparkSession sparkSession;
    private JavaSparkContext sc;
    private StructType structType;
    private static final String CACHE_PREFIX = "FRC/";

    public DistributedCache(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
        sc = new JavaSparkContext(sparkSession.sparkContext());
        igniteContext = new JavaIgniteContext<Long, FeatureRecord>(sc, "immunizer/ignite-cfg.xml");
        structType = new StructType();
        structType.add("id", DataTypes.LongType);
        structType.add("features", new VectorUDT());
    }

    public void save(String context, JavaPairRDD<Long, FeatureRecord> recordsToBeSavedRDD) {
        JavaIgniteRDD<Long, FeatureRecord> featureRecordRDD = igniteContext.fromCache(CACHE_PREFIX + context);
        featureRecordRDD.savePairs(recordsToBeSavedRDD);
    }

    public Dataset<Row> fetch(String context) {
        JavaPairRDD<Long, FeatureRecord> fetchedRecordsRDD = igniteContext.fromCache(CACHE_PREFIX + context);
        JavaRDD<Row> rowRDD = fetchedRecordsRDD.map(record -> {
            return RowFactory.create(record._1, record._2.getRecord().values());
        });

        return sparkSession.createDataFrame(rowRDD, structType);
    }

    public FeatureRecord get(String context, long key) {
        JavaPairRDD<Long, FeatureRecord> fetchedRecordsRDD = igniteContext.fromCache(CACHE_PREFIX + context);
        return fetchedRecordsRDD.filter(rec -> rec._1 == key).map(rec -> rec._2).first();
    }

    public void purge(String context, long minKey) {
        igniteContext.fromCache(CACHE_PREFIX + context).sql("delete from ?? where _key < ?", CACHE_PREFIX, context,
                minKey);
    }

    public void delete(String context, long key) {
        igniteContext.fromCache(CACHE_PREFIX + context).sql("delete from ?? where _key = ?", CACHE_PREFIX, context,
                key);
    }
}