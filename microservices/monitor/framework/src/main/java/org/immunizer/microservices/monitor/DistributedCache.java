package org.immunizer.microservices.monitor;

import org.apache.ignite.spark.JavaIgniteContext;
import org.apache.ignite.spark.JavaIgniteRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.StatCounter;

import scala.Tuple2;

public class DistributedCache {

    private JavaIgniteContext<String, Double> igniteContext1;
    private JavaIgniteContext<String, Long> igniteContext2;
    private JavaIgniteRDD<String, Double> numbersStdevsRDD, numbersMeansRDD, stringLengthsStdevsRDD,
            stringLengthsMeansRDD, wholeLengthsStdevsRDD, wholeLengthsMeansRDD, splits1MinFreqSumRDD,
            splits3MinFreqSumRDD;
    private JavaIgniteRDD<String, Long> callStacksRDD, numbersCountsRDD, wholeLengthsCountsRDD, stringLengthsCountsRDD,
            pathsRDD, aggPathsRDD, splits1RDD, splits3RDD;

    public DistributedCache(JavaSparkContext sc) {
        igniteContext1 = new JavaIgniteContext<String, Double>(sc, "immunizer/ignite-cfg.xml");
        igniteContext2 = new JavaIgniteContext<String, Long>(sc, "immunizer/ignite-cfg.xml");

        numbersStdevsRDD = igniteContext1.fromCache("numbersStdevs");
        numbersMeansRDD = igniteContext1.fromCache("numbersMeans");
        numbersCountsRDD = igniteContext2.fromCache("numbersCounts");
        stringLengthsStdevsRDD = igniteContext1.fromCache("stringLengthsStdevs");
        stringLengthsMeansRDD = igniteContext1.fromCache("stringLengthsMeans");
        stringLengthsCountsRDD = igniteContext2.fromCache("stringLengthsCounts");
        wholeLengthsStdevsRDD = igniteContext1.fromCache("wholeLengthsStdevs");
        wholeLengthsMeansRDD = igniteContext1.fromCache("wholeLengthsMeans");
        wholeLengthsCountsRDD = igniteContext2.fromCache("wholeLengthsCounts");
        callStacksRDD = igniteContext2.fromCache("callStacks");
        pathsRDD = igniteContext2.fromCache("paths");
        aggPathsRDD = igniteContext2.fromCache("aggPaths");
        splits1RDD = igniteContext2.fromCache("splits1");
        splits3RDD = igniteContext2.fromCache("splits3");
        /* For frequency smoothing */
        splits1MinFreqSumRDD = igniteContext1.fromCache("splits1MinFreqSum");
        splits3MinFreqSumRDD = igniteContext1.fromCache("splits3MinFreqSum");
    }

    public void updateModel(JavaRDD<String> model) {
        JavaPairRDD<String, StatCounter> numbersModel = model.filter(record -> record.startsWith("numbers_"))
                .mapToPair(record -> {
                    String key = record.substring(8, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).aggregateByKey(new StatCounter(), StatCounter::merge, StatCounter::merge);
        JavaPairRDD<String, Double> numbersStdevsModel = numbersModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().stdev()));
        JavaPairRDD<String, Double> numbersMeansModel = numbersModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().mean()));
        JavaPairRDD<String, Long> numbersCountsModel = numbersModel
                .mapToPair(stats -> new Tuple2<String, Long>(stats._1(), stats._2().count()));

        JavaPairRDD<String, StatCounter> stringLengthsModel = model.filter(record -> record.startsWith("strlens_"))
                .mapToPair(record -> {
                    String key = record.substring(8, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).aggregateByKey(new StatCounter(), StatCounter::merge, StatCounter::merge);
        JavaPairRDD<String, Double> stringLengthsStdevsModel = stringLengthsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().stdev()));
        JavaPairRDD<String, Double> stringLengthsMeansModel = stringLengthsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().mean()));
        JavaPairRDD<String, Long> stringLengthsCountsModel = stringLengthsModel
                .mapToPair(stats -> new Tuple2<String, Long>(stats._1(), stats._2().count()));

        JavaPairRDD<String, StatCounter> wholeLengthsModel = model.filter(record -> record.startsWith("whllens_"))
                .mapToPair(record -> {
                    String key = record.substring(8, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).aggregateByKey(new StatCounter(), StatCounter::merge, StatCounter::merge);
        JavaPairRDD<String, Double> wholeLengthsStdevsModel = wholeLengthsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().stdev()));
        JavaPairRDD<String, Double> wholeLengthsMeansModel = wholeLengthsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().mean()));
        JavaPairRDD<String, Long> wholeLengthsCountsModel = wholeLengthsModel
                .mapToPair(stats -> new Tuple2<String, Long>(stats._1(), stats._2().count()));

        JavaPairRDD<String, Long> callStacksModel = model.filter(record -> record.startsWith("callStacks_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(11), (long) 1))
                .reduceByKey((a, b) -> a + b);
        JavaPairRDD<String, Long> pathsModel = model.filter(record -> record.startsWith("paths_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(6), (long) 1))
                .reduceByKey((a, b) -> a + b);
        JavaPairRDD<String, Long> aggPathsModel = model.filter(record -> record.startsWith("aggpaths_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(9), (long) 1))
                .reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Long> splits1Model = model.filter(record -> record.startsWith("splits1_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(8), (long) 1))
                .reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Long> splits3Model = model.filter(record -> record.startsWith("splits3_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(8), (long) 1))
                .reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Double> splits1MinFreqSumModel = model
                .filter(record -> record.startsWith("splits1MinFreqSum_")).mapToPair(record -> {
                    String key = record.substring(18, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Double> splits3MinFreqSumModel = model
                .filter(record -> record.startsWith("splits3MinFreqSum_")).mapToPair(record -> {
                    String key = record.substring(18, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).reduceByKey((a, b) -> a + b);

        updateCountRDD(callStacksModel, callStacksRDD);
        updateCountRDD(pathsModel, pathsRDD);
        updateCountRDD(aggPathsModel, aggPathsRDD);
        updateCountRDD(splits1Model, splits1RDD);
        updateCountRDD(splits3Model, splits3RDD);

        updateSumRDD(splits1MinFreqSumModel, splits1MinFreqSumRDD);
        updateSumRDD(splits3MinFreqSumModel, splits3MinFreqSumRDD);

        updateMeanStdevCountRDD(numbersMeansModel, numbersStdevsModel, numbersCountsModel, numbersMeansRDD,
                numbersStdevsRDD, numbersCountsRDD);
        updateMeanStdevCountRDD(stringLengthsMeansModel, stringLengthsStdevsModel, stringLengthsCountsModel,
                stringLengthsMeansRDD, stringLengthsStdevsRDD, stringLengthsCountsRDD);
        updateMeanStdevCountRDD(wholeLengthsMeansModel, wholeLengthsStdevsModel, wholeLengthsCountsModel,
                wholeLengthsMeansRDD, wholeLengthsStdevsRDD, wholeLengthsCountsRDD);

        numbersStdevsRDD.foreach(entry -> {
            System.out.println("STDEV: " + entry._1() + ": " + entry._2());
        });
        numbersMeansRDD.foreach(entry -> {
            System.out.println("MEAN: " + entry._1() + ": " + entry._2());
        });
        pathsRDD.foreach(entry -> {
            System.out.println("PATH: " + entry._1() + ": " + entry._2());
        });
        aggPathsRDD.foreach(entry -> {
            System.out.println("AGGPATH: " + entry._1() + ": " + entry._2());
        });
        splits1RDD.foreach(entry -> {
            System.out.println("SPLIT1: " + entry._1() + ": " + entry._2());
        });
        splits3RDD.foreach(entry -> {
            System.out.println("SPLIT3: " + entry._1() + ": " + entry._2());
        });
    }

    private void updateCountRDD(JavaPairRDD<String, Long> model, JavaIgniteRDD<String, Long> rdd) {
        JavaPairRDD<String, Long> mappedModel = model.mapToPair(record -> {
            JavaPairRDD<String, Long> frdd = rdd.filter(rec -> rec._1().equals(record._1()));
            long value = record._2().longValue();
            if (frdd.count() > 0) {
                value += frdd.first()._2().longValue();
            }
            return new Tuple2<String, Long>(record._1(), value);
        });
        rdd.savePairs(mappedModel);
    }

    private void updateSumRDD(JavaPairRDD<String, Double> model, JavaIgniteRDD<String, Double> rdd) {
        JavaPairRDD<String, Double> mappedModel = model.mapToPair(record -> {
            JavaPairRDD<String, Double> frdd = rdd.filter(rec -> rec._1().equals(record._1()));
            double value = record._2().doubleValue();
            if (frdd.count() > 0) {
                value += frdd.first()._2().doubleValue();
            }
            return new Tuple2<String, Double>(record._1(), value);
        });
        rdd.savePairs(mappedModel);
    }

    private void updateMeanStdevCountRDD(JavaPairRDD<String, Double> meansModel,
            JavaPairRDD<String, Double> stdevsModel, JavaPairRDD<String, Long> countsModel,
            JavaIgniteRDD<String, Double> meansRDD, JavaIgniteRDD<String, Double> stdevsRDD,
            JavaIgniteRDD<String, Long> countsRDD) {
        JavaPairRDD<String, Double> mappedMeansModel = meansModel.mapToPair(record -> {
            double newMean = record._2().doubleValue();
            JavaPairRDD<String, Double> frdd = meansRDD.filter(rec -> rec._1().equals(record._1()));
            if (frdd.count() > 0) {
                double oldMean = frdd.first()._2().doubleValue();
                long oldCount = countsRDD.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                long newCount = countsModel.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                newMean = (oldMean * oldCount + newMean * newCount) / (oldCount + newCount);
            }
            return new Tuple2<String, Double>(record._1(), newMean);
        });
        JavaPairRDD<String, Double> mappedStdevsModel = stdevsModel.mapToPair(record -> {
            double newStdev = record._2().doubleValue();
            JavaPairRDD<String, Double> frdd = stdevsRDD.filter(rec -> rec._1().equals(record._1()));
            if (frdd.count() > 0) {
                double oldMean = meansRDD.filter(rec -> rec._1().equals(record._1())).first()._2().doubleValue();
                double oldStdev = frdd.first()._2().doubleValue();
                long oldCount = countsRDD.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                double newMean = meansModel.filter(r -> r._1().equals(record._1())).first()._2().doubleValue();
                long newCount = countsModel.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                double newCalculatedMean = (oldMean * oldCount + newMean * newCount) / (oldCount + newCount);
                double newVariance = (oldCount * (oldStdev * oldStdev + oldMean * oldMean)
                        + newCount * (newStdev * newStdev + newMean * newMean)) / (oldCount + newCount)
                        - newCalculatedMean * newCalculatedMean;
                newStdev = Math.sqrt(newVariance);
            }
            return new Tuple2<String, Double>(record._1(), newStdev);
        });
        JavaPairRDD<String, Long> mappedCountsModel = countsModel.mapToPair(record -> {
            long newCount = record._2().longValue();
            JavaPairRDD<String, Long> frdd = countsRDD.filter(rec -> rec._1().equals(record._1()));
            if (frdd.count() > 0) {
                newCount += frdd.first()._2().longValue();
            }
            return new Tuple2<String, Long>(record._1(), newCount);
        });
        meansRDD.savePairs(mappedMeansModel);
        stdevsRDD.savePairs(mappedStdevsModel);
        countsRDD.savePairs(mappedCountsModel);
    }
}