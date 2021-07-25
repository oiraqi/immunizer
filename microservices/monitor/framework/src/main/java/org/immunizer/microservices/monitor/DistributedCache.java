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
    private JavaIgniteRDD<String, Double> numbersStdevsRDD;
    private JavaIgniteRDD<String, Double> numbersMeansRDD;
    private JavaIgniteRDD<String, Double> stringLengthsStdevsRDD;
    private JavaIgniteRDD<String, Double> stringLengthsMeansRDD;
    private JavaIgniteRDD<String, Double> wholeLengthsStdevsRDD;
    private JavaIgniteRDD<String, Double> wholeLengthsMeansRDD;
    private JavaIgniteRDD<String, Double> splits1MinFreqsStdevsRDD;
    private JavaIgniteRDD<String, Double> splits1MinFreqsMeansRDD;
    private JavaIgniteRDD<String, Double> splits3MinFreqsStdevsRDD;
    private JavaIgniteRDD<String, Double> splits3MinFreqsMeansRDD;
    private JavaIgniteRDD<String, Double> splits1MinFreqSumRDD;
    private JavaIgniteRDD<String, Double> splits3MinFreqSumRDD;
    private JavaIgniteRDD<String, Long> contextsCountsRDD;
    private JavaIgniteRDD<String, Long> numbersCountsRDD;
    private JavaIgniteRDD<String, Long> stringLengthsCountsRDD;
    private JavaIgniteRDD<String, Long> pathsRDD;
    private JavaIgniteRDD<String, Long> aggPathsRDD;
    private JavaIgniteRDD<String, Long> splits1RDD;
    private JavaIgniteRDD<String, Long> splits3RDD;

    public DistributedCache(JavaSparkContext sc) {

        igniteContext1 = new JavaIgniteContext<>(sc, "immunizer/ignite-cfg.xml");
        igniteContext2 = new JavaIgniteContext<>(sc, "immunizer/ignite-cfg.xml");

        numbersStdevsRDD = igniteContext1.fromCache("numbersStdevs");
        numbersMeansRDD = igniteContext1.fromCache("numbersMeans");
        numbersCountsRDD = igniteContext2.fromCache("numbersCounts");
        stringLengthsStdevsRDD = igniteContext1.fromCache("stringLengthsStdevs");
        stringLengthsMeansRDD = igniteContext1.fromCache("stringLengthsMeans");
        stringLengthsCountsRDD = igniteContext2.fromCache("stringLengthsCounts");
        wholeLengthsStdevsRDD = igniteContext1.fromCache("wholeLengthsStdevs");
        wholeLengthsMeansRDD = igniteContext1.fromCache("wholeLengthsMeans");
        splits1MinFreqsStdevsRDD = igniteContext1.fromCache("splits1MinFreqsStdevs");
        splits1MinFreqsMeansRDD = igniteContext1.fromCache("splits1MinFreqsMeans");
        splits3MinFreqsStdevsRDD = igniteContext1.fromCache("splits3MinFreqsStdevs");
        splits3MinFreqsMeansRDD = igniteContext1.fromCache("splits3MinFreqsMeans");
        contextsCountsRDD = igniteContext2.fromCache("contextsCounts");
        pathsRDD = igniteContext2.fromCache("paths");
        aggPathsRDD = igniteContext2.fromCache("aggPaths");
        splits1RDD = igniteContext2.fromCache("splits1");
        splits3RDD = igniteContext2.fromCache("splits3");
        
        /* For frequency smoothing */
        splits1MinFreqSumRDD = igniteContext1.fromCache("splits1MinFreqsSum");
        splits3MinFreqSumRDD = igniteContext1.fromCache("splits3MinFreqsSum");
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

        JavaPairRDD<String, StatCounter> splits1MinFreqsModel = model.filter(record -> record.startsWith("splits1MinFreqs_"))
                .mapToPair(record -> {
                    var key = record.substring(16, record.lastIndexOf('_'));
                    var value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).aggregateByKey(new StatCounter(), StatCounter::merge, StatCounter::merge);
        JavaPairRDD<String, Double> splits1MinFreqsStdevsModel = splits1MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().stdev()));
        JavaPairRDD<String, Double> splits1MinFreqsMeansModel = splits1MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().mean()));
        JavaPairRDD<String, Long> splits1MinFreqsCountsModel = splits1MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Long>(stats._1(), stats._2().count()));

        JavaPairRDD<String, StatCounter> splits3MinFreqsModel = model.filter(record -> record.startsWith("splits3MinFreqs_"))
                .mapToPair(record -> {
                    String key = record.substring(16, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).aggregateByKey(new StatCounter(), StatCounter::merge, StatCounter::merge);
        JavaPairRDD<String, Double> splits3MinFreqsStdevsModel = splits3MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().stdev()));
        JavaPairRDD<String, Double> splits3MinFreqsMeansModel = splits3MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Double>(stats._1(), stats._2().mean()));
        JavaPairRDD<String, Long> splits3MinFreqsCountsModel = splits3MinFreqsModel
                .mapToPair(stats -> new Tuple2<String, Long>(stats._1(), stats._2().count()));

        JavaPairRDD<String, Long> contextsCountsModel = model.filter(record -> record.startsWith("contexts_"))
                .mapToPair(record -> new Tuple2<String, Long>(record.substring(9), (long) 1))
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
                .filter(record -> record.startsWith("splits1MinFreqs_")).mapToPair(record -> {
                    String key = record.substring(16, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).reduceByKey((a, b) -> a + b);

        JavaPairRDD<String, Double> splits3MinFreqSumModel = model
                .filter(record -> record.startsWith("splits3MinFreqs_")).mapToPair(record -> {
                    String key = record.substring(16, record.lastIndexOf('_'));
                    Double value = Double.valueOf(record.substring(record.lastIndexOf('_') + 1));
                    return new Tuple2<String, Double>(key, value);
                }).reduceByKey((a, b) -> a + b);

        updateCountRDD(contextsCountsModel, contextsCountsRDD);
        updateCountRDD(pathsModel, pathsRDD);
        updateCountRDD(aggPathsModel, aggPathsRDD);
        updateCountRDD(splits1Model, splits1RDD);
        updateCountRDD(splits3Model, splits3RDD);

        updateSumRDD(splits1MinFreqSumModel, splits1MinFreqSumRDD);
        updateSumRDD(splits3MinFreqSumModel, splits3MinFreqSumRDD);

        updateMeanStdevCountRDD(numbersMeansModel, numbersStdevsModel, numbersCountsModel, numbersMeansRDD,
                numbersStdevsRDD, numbersCountsRDD, true);
        updateMeanStdevCountRDD(stringLengthsMeansModel, stringLengthsStdevsModel, stringLengthsCountsModel,
                stringLengthsMeansRDD, stringLengthsStdevsRDD, stringLengthsCountsRDD, true);
        updateMeanStdevCountRDD(wholeLengthsMeansModel, wholeLengthsStdevsModel, wholeLengthsCountsModel,
                wholeLengthsMeansRDD, wholeLengthsStdevsRDD, contextsCountsRDD, false);
        updateMeanStdevCountRDD(splits1MinFreqsMeansModel, splits1MinFreqsStdevsModel, splits1MinFreqsCountsModel, splits1MinFreqsMeansRDD,
                splits1MinFreqsStdevsRDD, contextsCountsRDD, false);
        updateMeanStdevCountRDD(splits3MinFreqsMeansModel, splits3MinFreqsStdevsModel, splits3MinFreqsCountsModel, splits3MinFreqsMeansRDD,
                splits3MinFreqsStdevsRDD, contextsCountsRDD, false);

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
            JavaIgniteRDD<String, Long> countsRDD, boolean updateCounts) {
        JavaPairRDD<String, Double> mappedMeansModel = meansModel.mapToPair(record -> {
            var newMean = record._2().doubleValue();
            JavaPairRDD<String, Double> frdd = meansRDD.filter(rec -> rec._1().equals(record._1()));
            if (frdd.count() > 0) {
                var oldMean = frdd.first()._2().doubleValue();
                var oldCount = countsRDD.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                var newCount = countsModel.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                newMean = (oldMean * oldCount + newMean * newCount) / (oldCount + newCount);
            }
            return new Tuple2<String, Double>(record._1(), newMean);
        });
        JavaPairRDD<String, Double> mappedStdevsModel = stdevsModel.mapToPair(record -> {
            var newStdev = record._2().doubleValue();
            JavaPairRDD<String, Double> frdd = stdevsRDD.filter(rec -> rec._1().equals(record._1()));
            if (frdd.count() > 0) {
                var oldMean = meansRDD.filter(rec -> rec._1().equals(record._1())).first()._2().doubleValue();
                var oldStdev = frdd.first()._2().doubleValue();
                var oldCount = countsRDD.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                var newMean = meansModel.filter(r -> r._1().equals(record._1())).first()._2().doubleValue();
                var newCount = countsModel.filter(r -> r._1().equals(record._1())).first()._2().longValue();
                double newCalculatedMean = (oldMean * oldCount + newMean * newCount) / (oldCount + newCount);
                double newVariance = (oldCount * (oldStdev * oldStdev + oldMean * oldMean)
                        + newCount * (newStdev * newStdev + newMean * newMean)) / (oldCount + newCount)
                        - newCalculatedMean * newCalculatedMean;
                newStdev = Math.sqrt(newVariance);
            }
            return new Tuple2<String, Double>(record._1(), newStdev);
        });        
        meansRDD.savePairs(mappedMeansModel);
        stdevsRDD.savePairs(mappedStdevsModel);

        if (updateCounts) {
            JavaPairRDD<String, Long> mappedCountsModel = countsModel.mapToPair(record -> {
                var newCount = record._2().longValue();
                JavaPairRDD<String, Long> frdd = countsRDD.filter(rec -> rec._1().equals(record._1()));
                if (frdd.count() > 0) {
                    newCount += frdd.first()._2().longValue();
                }
                return new Tuple2<String, Long>(record._1(), newCount);
            });        
            countsRDD.savePairs(mappedCountsModel);
        }
    }
}