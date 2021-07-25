package org.immunizer.microservices.monitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonParser;

import org.apache.spark.api.java.function.FlatMapFunction;

import com.google.gson.JsonElement;
import com.google.common.base.Splitter;

import org.apache.ignite.Ignition;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Collections;

public class ModelMapper implements FlatMapFunction<byte[], String> {

	private static final long serialVersionUID = 1L;
	private transient IgniteCache<String, Double> numbersStdevsCache;
	private transient IgniteCache<String, Double> numbersMeansCache;
	private transient IgniteCache<String, Double> stringLengthsStdevsCache;
	private transient IgniteCache<String, Double> stringLengthsMeansCache;
	private transient IgniteCache<String, Double> wholeLengthsStdevsCache;
	private transient IgniteCache<String, Double> wholeLengthsMeansCache;
	private transient IgniteCache<String, Double> splits1MinFreqsStdevsCache;
	private transient IgniteCache<String, Double> splits1MinFreqsMeansCache;
	private transient IgniteCache<String, Double> splits3MinFreqsStdevsCache;
	private transient IgniteCache<String, Double> splits3MinFreqsMeansCache;
	private transient IgniteCache<String, Double> splits1MinFreqsSumCache;
	private transient IgniteCache<String, Double> splits3MinFreqsSumCache;
	private transient IgniteCache<String, Long> contextsCountsCache;
	private transient IgniteCache<String, Long> pathsCache;
	private transient IgniteCache<String, Long> aggPathsCache;
	private transient IgniteCache<String, Long> splits1Cache;
	private transient IgniteCache<String, Long> splits3Cache;
	private long contextOccurences;
	private long[] minPathOccurences;
	private long[] min1Occurences;
	private long[] min3Occurences;
	private String[] min1AggregatedPathToNode;
	private String[] min3AggregatedPathToNode;
	private String[] min1Splits;
	private String[] min3Splits;
	private double[] maxNumberVariations;
	private double[] maxStringLengthVariations;
	private String[] maxSLVAggregatedPath;
	private String[] maxNVAggregatedPath;
	private double[] wholeLengthVariations;
	private transient JsonObject invocation;
	private int numberOfParams;
	private String callStackId;
	private String context;
	HashMap<String, String> model = new HashMap<>();
	HashMap<String, Double> record = new HashMap<>();
	HashMap<String, String> suspects = new HashMap<>();

	private static final double Z_SCORE_THRESHOLD = 3;

	/**
	 * Initilaizes Ignite Caches
	 */
	private void initCaches() {
		TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
		ipFinder.setAddresses(Collections.singletonList("ignite:47500..47509"));

		TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();
		discoSpi.setIpFinder(ipFinder);

		IgniteConfiguration cfg = new IgniteConfiguration();
		cfg.setDiscoverySpi(discoSpi);

		cfg.setIgniteInstanceName("Monitor");

		Ignite ignite = Ignition.getOrStart(cfg);
		numbersStdevsCache = ignite.getOrCreateCache("numbersStdevs");
		numbersMeansCache = ignite.getOrCreateCache("numbersMeans");
		stringLengthsStdevsCache = ignite.getOrCreateCache("stringLengthsStdevs");
		stringLengthsMeansCache = ignite.getOrCreateCache("stringLengthsMeans");
		wholeLengthsStdevsCache = ignite.getOrCreateCache("wholeLengthsStdevs");
		wholeLengthsMeansCache = ignite.getOrCreateCache("wholeLengthsMeans");
		splits1MinFreqsStdevsCache = ignite.getOrCreateCache("splits1MinFreqsStdevs");
		splits1MinFreqsMeansCache = ignite.getOrCreateCache("splits1MinFreqsMeans");
		splits3MinFreqsStdevsCache = ignite.getOrCreateCache("splits3MinFreqsStdevs");
		splits3MinFreqsMeansCache = ignite.getOrCreateCache("splits3MinFreqsMeans");
		contextsCountsCache = ignite.getOrCreateCache("contextsCounts");
		pathsCache = ignite.getOrCreateCache("paths");
		aggPathsCache = ignite.getOrCreateCache("aggPaths");
		splits1Cache = ignite.getOrCreateCache("splits1");
		splits3Cache = ignite.getOrCreateCache("splits3");
		splits1MinFreqsSumCache = ignite.getOrCreateCache("splits1MinFreqsSum");
		splits3MinFreqsSumCache = ignite.getOrCreateCache("splits3MinFreqsSum");
	}

	/**
	 * Initializes the model
	 */
	private void initModel() {
		callStackId = invocation.get("callStackId").getAsString();
		var prefix = invocation.get("swid").getAsString();
		if (invocation.get("cxid").getAsString() != null) {
			prefix += '_' + invocation.get("cxid").getAsString();
		}
		context = prefix + '_' + callStackId;
		model.put("contexts_" + context, "");
		contextOccurences = contextsCountsCache.get(context);
		minPathOccurences = new long[numberOfParams + 1];
		min1Occurences = new long[numberOfParams + 1];
		min3Occurences = new long[numberOfParams + 1];
		min1AggregatedPathToNode = new String[numberOfParams + 1];
		min3AggregatedPathToNode = new String[numberOfParams + 1];
		min1Splits = new String[numberOfParams];
		min3Splits = new String[numberOfParams];
		maxSLVAggregatedPath = new String[numberOfParams];
		maxNVAggregatedPath = new String[numberOfParams];
		wholeLengthVariations = new double[numberOfParams + 1];
		maxNumberVariations = new double[numberOfParams + 1];
		maxStringLengthVariations = new double[numberOfParams + 1];
	}

	/**
	 * Creates and Sends the Feature Record through the Message Broker, currently
	 * Kafka
	 */
	private void createAndSendFeatureRecord() {
		FeatureRecord fr = new FeatureRecord(callStackId, invocation.get("tag").getAsString(),
				invocation.get("label").getAsString(), invocation.get("fullyQualifiedMethodName").getAsString(),
				invocation.get("swid").getAsString(), invocation.get("cxid").getAsString(), record, suspects);
		new FeatureRecordProducer().send(fr);
	}

	/**
	 * Extracts features from invocation Uses build method to build features
	 * recursively for each parameter tree or returned value tree
	 * 
	 * @param invocation
	 * @return The Feature Record
	 */
	public Iterator<String> call(byte[] invocationBytes) {
		JsonParser parser = new JsonParser();
		invocation = parser.parse(new String(invocationBytes)).getAsJsonObject();
		JsonArray parameters = invocation.get("params").getAsJsonArray();
		numberOfParams = parameters.size();

		if (numberOfParams == 0)
			return null;

		int length;
		double splits1MinFrequenciesSum;
		double splits3MinFrequenciesSum;
		double wholeLengthMean;
		double wholeLengthStdev;

		initCaches();
		initModel();

		for (var i = 0; i < numberOfParams; i++) {
			JsonElement pi = parameters.get(i);
			length = pi.toString().length();
			model.put("whllens_" + context + "_p" + i + "_" + length, "");
			wholeLengthMean = wholeLengthsMeansCache.get(context + "_p" + i);
			wholeLengthStdev = wholeLengthsStdevsCache.get(context + "_p" + i);
			wholeLengthVariations[i] = Math.abs(length - wholeLengthMean) / wholeLengthStdev;
			minPathOccurences[i] = min1Occurences[i] = min3Occurences[i] = contextOccurences;
			maxNumberVariations[i] = maxStringLengthVariations[i] = 0.5;
			build("p" + i, "p" + i, pi, i);
			buildRecordFromParam(pi, i);

			splits1MinFrequenciesSum = splits1MinFreqsSumCache.get(context + "_p" + i);
			model.put("splits1MinFreqs_" + context + "_p" + i + "_"
					+ (double) min1Occurences[i] / aggPathsCache.get(context + "_" + min1AggregatedPathToNode[i]),
					"");
			/* Frequency smoothing */
			if (min1Occurences[i] > splits1MinFrequenciesSum) /* No need to divide both sides by contextOccurences */
				min1Occurences[i] = (long) splits1MinFrequenciesSum;

			splits3MinFrequenciesSum = splits3MinFreqsSumCache.get(context + "_p" + i);
			model.put("splits3MinFreqs_" + context + "_p" + i + "_"
					+ (double) min3Occurences[i] / aggPathsCache.get(context + "_" + min3AggregatedPathToNode[i]),
					"");
			/* Frequency smoothing */
			if (min3Occurences[i] > splits3MinFrequenciesSum)
				min3Occurences[i] = (long) splits3MinFrequenciesSum;
		}

		if (invocation.get("_returns").getAsBoolean()) {
			JsonElement result = invocation.get("result");
			length = result.toString().length();
			model.put("whllens_" + context + "_r_" + length, "");
			wholeLengthMean = wholeLengthsMeansCache.get(context + "_r");
			wholeLengthStdev = wholeLengthsStdevsCache.get(context + "_r");
			wholeLengthVariations[numberOfParams] = Math.abs(length - wholeLengthMean) / wholeLengthStdev;
			minPathOccurences[numberOfParams] = min3Occurences[numberOfParams] = min1Occurences[numberOfParams] = contextOccurences;
			maxNumberVariations[numberOfParams] = maxStringLengthVariations[numberOfParams] = 0.5;
			build("r", "r", result, numberOfParams);
			buildRecordFromResult(result);

			splits1MinFrequenciesSum = splits1MinFreqsSumCache.get(context + "_r");
			model.put(
					"splits1MinFreqs_" + context + "_r_"
							+ (double) min1Occurences[numberOfParams]
									/ aggPathsCache.get(context + "_" + min1AggregatedPathToNode[numberOfParams]),
					"");
			/* Frequency smoothing */
			if (min1Occurences[numberOfParams] > splits1MinFrequenciesSum) /*
																			 * No need to divide both sides by
																			 * contextOccurences
																			 */
				min1Occurences[numberOfParams] = (long) splits1MinFrequenciesSum;

			splits3MinFrequenciesSum = splits3MinFreqsSumCache.get(context + "_r");
			model.put(
					"splits3MinFreqs_" + context + "_r_"
							+ (double) min3Occurences[numberOfParams]
									/ aggPathsCache.get(context + "_" + min3AggregatedPathToNode[numberOfParams]),
					"");
			/* Frequency smoothing */
			if (min3Occurences[numberOfParams] > splits3MinFrequenciesSum)
				min3Occurences[numberOfParams] = (long) splits3MinFrequenciesSum;
		}

		createAndSendFeatureRecord();

		return model.keySet().iterator();
	}

	/**
	 * Builds features for each parameter or returned value by walking recursively
	 * through the parameter tree or returned value tree
	 * 
	 * @param callStackId
	 * @param pathToNode
	 * @param aggregatedPathToNode for sibling/relative grouping and comparision
	 * @param jsonElement
	 * @param model
	 */
	private void build(String pathToNode, String aggregatedPathToNode, JsonElement jsonElement, int paramIndex) {

		if (jsonElement.isJsonNull())
			return;

		if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++)
				/**
				 * While we call build for each element of the array with a different pathToNode
				 * (second parameter: pathToNode + '_' + i), we keep track of the same
				 * aggregatedPathToNode for all of them (third parameter) to group and compare
				 * siblings and relatives
				 */
				build(pathToNode + '_' + i, aggregatedPathToNode, jsonArray.get(i), paramIndex);
		} else if (jsonElement.isJsonObject()) {
			var jsonObject = jsonElement.getAsJsonObject();

			Iterator<Entry<String, JsonElement>> entries = jsonObject.entrySet().iterator();
			while (entries.hasNext()) {
				String key = entries.next().getKey();
				build(pathToNode.isEmpty() ? key : pathToNode + '_' + key,
						aggregatedPathToNode.isEmpty() ? key : aggregatedPathToNode + '_' + key, jsonObject.get(key),
						paramIndex);
			}
		} else {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			model.put("paths_" + context + "_" + pathToNode, "");
			long pathOccurences = pathsCache.get(context + "_" + pathToNode);
			if (pathOccurences < minPathOccurences[paramIndex])
				minPathOccurences[paramIndex] = pathOccurences;

			String key = "aggpaths_" + context + "_" + aggregatedPathToNode;
			if (!model.containsKey(key))
				model.put(key, "");

			if (primitive.isString()) {
				var value = primitive.getAsString();
				model.put("strlens_" + context + '_' + aggregatedPathToNode + '_' + value.length(), "");
				double stringLengthsMean = stringLengthsMeansCache.get(context + '_' + aggregatedPathToNode);
				double stringLengthsStdev = stringLengthsStdevsCache.get(context + '_' + aggregatedPathToNode);
				double stringLengthVariation = Math.abs(value.length() - stringLengthsMean) / stringLengthsStdev;
				if (stringLengthVariation > 1 && stringLengthVariation > maxStringLengthVariations[paramIndex]) {
					maxStringLengthVariations[paramIndex] = stringLengthVariation;
					if (stringLengthVariation > Z_SCORE_THRESHOLD) {
						maxSLVAggregatedPath[paramIndex] = aggregatedPathToNode;
					}
				}

				MinSplit minSplit;
				minSplit = getMinSplit(value, 1, context, aggregatedPathToNode);
				if (minSplit.occurences < min1Occurences[paramIndex]) {
					min1Occurences[paramIndex] = minSplit.occurences;
					min1AggregatedPathToNode[paramIndex] = aggregatedPathToNode;
					min1Splits[paramIndex] = minSplit.split;
				}
				minSplit = getMinSplit(value, 3, context, aggregatedPathToNode);
				if (minSplit.occurences < min3Occurences[paramIndex]) {
					min3Occurences[paramIndex] = minSplit.occurences;
					min3AggregatedPathToNode[paramIndex] = aggregatedPathToNode;
					min3Splits[paramIndex] = minSplit.split;
				}
			} else if (primitive.isNumber()) {
				var value = primitive.getAsNumber().doubleValue();
				model.put("numbers_" + context + '_' + aggregatedPathToNode + '_' + value, "");
				double numbersMean = numbersMeansCache.get(context + '_' + aggregatedPathToNode);
				double numbersStdev = numbersStdevsCache.get(context + '_' + aggregatedPathToNode);
				double numberVariation = Math.abs(value - numbersMean) / numbersStdev;
				if (numberVariation > 1 && numberVariation > maxNumberVariations[paramIndex]) {
					maxNumberVariations[paramIndex] = numberVariation;
					if (numberVariation > Z_SCORE_THRESHOLD) {
						maxNVAggregatedPath[paramIndex] = aggregatedPathToNode;
					}
				}
			}
		}
	}

	private MinSplit getMinSplit(String input, int n, String context, String aggregatedPathToNode) {
		Splitter splitter = Splitter.fixedLength(n);
		long minSplitOccurences = -1;
		String minSplit = "";

		for (var i = 0; i < n && i < input.length(); i++) {
			Iterable<String> splits = splitter.split(input.substring(i));

			for (String split : splits) {
				String key = "splits" + n + "_" + context + "_" + aggregatedPathToNode + "_" + split;
				if (!model.containsKey(key))
					model.put(key, "");

				long splitOccurences = 0;
				if (n == 1)
					splitOccurences = splits1Cache.get(context + "_" + aggregatedPathToNode + "_" + split);
				else if (n == 3)
					splitOccurences = splits3Cache.get(context + "_" + aggregatedPathToNode + "_" + split);

				if ((minSplitOccurences >= 0 && splitOccurences < minSplitOccurences) || minSplitOccurences == -1) {
					minSplitOccurences = splitOccurences;
					minSplit = split;
				}
			}
		}
		return new MinSplit(minSplit, minSplitOccurences);
	}

	private void buildRecordFromParam(JsonElement pi, int i) {
		if (!pi.isJsonPrimitive()) {
			record.put("p" + i + "_length_variation", wholeLengthVariations[i]);
			if (wholeLengthVariations[i] > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_5", "");
			}
			record.put(i + "_path_min_f", (double) minPathOccurences[i] / contextOccurences);

			var min1FreqsMean = splits1MinFreqsMeansCache.get(context + "_p" + i);
			var min1FreqsStdev = splits1MinFreqsStdevsCache.get(context + "_p" + i);
			var min1Freq = (double) min1Occurences[i] / aggPathsCache.get(context + "_" + min1AggregatedPathToNode[i]);
			var min1FreqVariation = Math.abs(min1Freq - min1FreqsMean) / min1FreqsStdev;
			record.put("p" + i + "_min_if1_variation", min1FreqVariation);
			if (min1FreqVariation > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_21", min1AggregatedPathToNode[i] + '/' + min1Splits[i]);
			}

			var min3FreqsMean = splits3MinFreqsMeansCache.get(context + "_p" + i);
			var min3FreqsStdev = splits3MinFreqsStdevsCache.get(context + "_p" + i);
			var min3Freq = (double) min3Occurences[i] / aggPathsCache.get(context + "_" + min3AggregatedPathToNode[i]);
			var min3FreqVariation = Math.abs(min3Freq - min3FreqsMean) / min3FreqsStdev;
			record.put("p" + i + "_min_if3_variation", min3FreqVariation);
			if (min3FreqVariation > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_23", min3AggregatedPathToNode[i] + '/' + min3Splits[i]);
			}

			record.put("p" + i + "_max_string_length_variation", maxStringLengthVariations[i]);
			if (maxStringLengthVariations[i] > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_3", maxSLVAggregatedPath[i]);
			}

			record.put("p" + i + "_max_number_variation", maxNumberVariations[i]);
			if (maxNumberVariations[i] > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_4", maxNVAggregatedPath[i]);
			}
		} else if (pi.getAsJsonPrimitive().isString()) {
			var min1FreqsMean = splits1MinFreqsMeansCache.get(context + "_p" + i);
			var min1FreqsStdev = splits1MinFreqsStdevsCache.get(context + "_p" + i);
			var min1Freq = (double) min1Occurences[i] / aggPathsCache.get(context + "_" + min1AggregatedPathToNode[i]);
			var min1FreqVariation = Math.abs(min1Freq - min1FreqsMean) / min1FreqsStdev;
			record.put("p" + i + "_min_if1_variation", min1FreqVariation);
			if (min1FreqVariation > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_21", min1AggregatedPathToNode[i] + '/' + min1Splits[i]);
			}
			
			var min3FreqsMean = splits3MinFreqsMeansCache.get(context + "_p" + i);
			var min3FreqsStdev = splits3MinFreqsStdevsCache.get(context + "_p" + i);
			var min3Freq = (double) min3Occurences[i] / aggPathsCache.get(context + "_" + min3AggregatedPathToNode[i]);
			var min3FreqVariation = Math.abs(min3Freq - min3FreqsMean) / min3FreqsStdev;
			record.put("p" + i + "_min_if3_variation", min3FreqVariation);
			if (min3FreqVariation > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_23", min3AggregatedPathToNode[i] + '/' + min3Splits[i]);
			}

			record.put("p" + i + "_length_variation", maxStringLengthVariations[i]);
			if (wholeLengthVariations[i] > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_3", "");
			}
		} else if (pi.getAsJsonPrimitive().isNumber()) {
			record.put("p" + i + "_number_variation", maxNumberVariations[i]);
			if (maxNumberVariations[i] > Z_SCORE_THRESHOLD) {
				suspects.put(i + "_4", "");
			}
		}
	}

	private void buildRecordFromResult(JsonElement result) {
		if (invocation.get("_returnsString").getAsBoolean()) {
			var min1FreqsMean = splits1MinFreqsMeansCache.get(context + "_r");
			var min1FreqsStdev = splits1MinFreqsStdevsCache.get(context + "_r");
			var min1Freq = (double) min1Occurences[numberOfParams] / aggPathsCache.get(context + "_" + min1AggregatedPathToNode[numberOfParams]);
			var min1FreqVariation = Math.abs(min1Freq - min1FreqsMean) / min1FreqsStdev;
			record.put("r_min_if1_variation", min1FreqVariation);
			
			var min3FreqsMean = splits3MinFreqsMeansCache.get(context + "_r");
			var min3FreqsStdev = splits3MinFreqsStdevsCache.get(context + "_r");
			var min3Freq = (double) min3Occurences[numberOfParams] / aggPathsCache.get(context + "_" + min3AggregatedPathToNode[numberOfParams]);
			var min3FreqVariation = Math.abs(min3Freq - min3FreqsMean) / min3FreqsStdev;
			record.put("r_min_if3_variation", min3FreqVariation);
			
			record.put("r_length_variation", maxStringLengthVariations[numberOfParams]);
		} else if (result.isJsonPrimitive() && result.getAsJsonPrimitive().isNumber()) {
			record.put("r_number_variation", maxNumberVariations[numberOfParams]);
		} else {
			record.put("r_length_variation", wholeLengthVariations[numberOfParams]);
			record.put("r_path_min_f", (double) minPathOccurences[numberOfParams] / contextOccurences);
			
			var min1FreqsMean = splits1MinFreqsMeansCache.get(context + "_r");
			var min1FreqsStdev = splits1MinFreqsStdevsCache.get(context + "_r");
			var min1Freq = (double) min1Occurences[numberOfParams] / aggPathsCache.get(context + "_" + min1AggregatedPathToNode[numberOfParams]);
			var min1FreqVariation = Math.abs(min1Freq - min1FreqsMean) / min1FreqsStdev;
			record.put("r_min_if1_variation", min1FreqVariation);
			
			var min3FreqsMean = splits3MinFreqsMeansCache.get(context + "_r");
			var min3FreqsStdev = splits3MinFreqsStdevsCache.get(context + "_r");
			var min3Freq = (double) min3Occurences[numberOfParams] / aggPathsCache.get(context + "_" + min3AggregatedPathToNode[numberOfParams]);
			var min3FreqVariation = Math.abs(min3Freq - min3FreqsMean) / min3FreqsStdev;
			record.put("r_min_if3_variation", min3FreqVariation);
			
			record.put("r_max_string_length_variation", maxStringLengthVariations[numberOfParams]);
			record.put("r_max_number_variation", maxNumberVariations[numberOfParams]);
		}
	}

	private class MinSplit {
		public String split;
		public long occurences;

		public MinSplit(String split, long occurences) {
			this.split = split;
			this.occurences = occurences;
		}
	}
}