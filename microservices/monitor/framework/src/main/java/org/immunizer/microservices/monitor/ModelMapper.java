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
	private Ignite ignite;
	private IgniteCache<String, Double> numbersStdevsCache, numbersMeansCache, stringLengthsStdevsCache,
			stringLengthsMeansCache, wholeLengthsStdevsCache, wholeLengthsMeansCache, splits1MinFrequenciesCache,
			splits3MinFrequenciesCache;
	private IgniteCache<String, Long> callStacksCache, pathsCache, aggPathsCache, splits1Cache, splits3Cache;
	private long callStackOccurences;
	private long[] minPathOccurences, min1Occurences, min3Occurences;
	private String[] min1AggregatedPathToNode, min3AggregatedPathToNode;
	private double[] maxNumberVariations, maxStringLengthVariations, wholeLengthVariations;
	private JsonObject invocation;
	private int numberOfParams;
	private String callStackId;
	HashMap<String, String> model = new HashMap<String, String>();
	HashMap<String, Double> record = new HashMap<String, Double>();

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

		ignite = Ignition.getOrStart(cfg);
		numbersStdevsCache = ignite.cache("numbersStdevs");
		numbersMeansCache = ignite.cache("numbersMeans");
		stringLengthsStdevsCache = ignite.cache("stringLengthsStdevs");
		stringLengthsMeansCache = ignite.cache("stringLengthsMeans");
		wholeLengthsStdevsCache = ignite.cache("wholeLengthsStdevs");
		wholeLengthsMeansCache = ignite.cache("wholeLengthsMeans");
		callStacksCache = ignite.cache("callStacks");
		pathsCache = ignite.cache("paths");
		aggPathsCache = ignite.cache("aggPaths");
		splits1Cache = ignite.cache("splits1");
		splits3Cache = ignite.cache("splits3");
		splits1MinFrequenciesCache = ignite.cache("splits1MinFreqSum");
		splits3MinFrequenciesCache = ignite.cache("splits3MinFreqSum");
	}

	/**
	 * Initializes the model
	 */
	private void initModel() {
		callStackId = invocation.get("callStackId").getAsString();
		model.put("callstacks_" + callStackId, "");
		callStackOccurences = callStacksCache.get(callStackId);
		minPathOccurences = new long[numberOfParams + 1];
		min1Occurences = new long[numberOfParams + 1];
		min3Occurences = new long[numberOfParams + 1];
		min1AggregatedPathToNode = new String[numberOfParams + 1];
		min3AggregatedPathToNode = new String[numberOfParams + 1];
		wholeLengthVariations = new double[numberOfParams + 1];
		maxNumberVariations = new double[numberOfParams + 1];
		maxStringLengthVariations = new double[numberOfParams + 1];
	}

	/**
	 * Creates and Sends the Feature Record through the Message Broker, currently
	 * Kafka
	 */
	private void createAndSendFeatureRecord() {
		FeatureRecord fr = new FeatureRecord(callStackId, invocation.get("threadTag").getAsString(),
				invocation.get("fullyQualifiedMethodName").getAsString(), invocation.get("swid").getAsString(), record);
		FeatureRecordProducer frp = new FeatureRecordProducer(invocation.get("swid").getAsString());
		frp.send(fr);
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
		double splits1MinFrequenciesSum, splits3MinFrequenciesSum, wholeLengthMean, wholeLengthStdev;

		initCaches();
		initModel();

		for (int i = 0; i < numberOfParams; i++) {
			JsonElement pi = parameters.get(i);
			length = pi.toString().length();
			model.put("whllens_" + callStackId + "_p" + i + "_" + length, "");
			wholeLengthMean = wholeLengthsMeansCache.get(callStackId + "_p" + i);
			wholeLengthStdev = wholeLengthsStdevsCache.get(callStackId + "_p" + i);
			wholeLengthVariations[i] = Math.abs(length - wholeLengthMean) / wholeLengthStdev;
			minPathOccurences[i] = min1Occurences[i] = min3Occurences[i] = callStackOccurences;
			maxNumberVariations[i] = maxStringLengthVariations[i] = 0.5;
			build("p" + i, "p" + i, pi, i);
			buildRecordFromParam(pi, i);

			splits1MinFrequenciesSum = splits1MinFrequenciesCache.get("" + callStackId + "_p" + i);
			model.put("splits1MinFreqSum_" + callStackId + "_p" + i + "_"
					+ (double) min1Occurences[i] / aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[i]),
					"");
			/* Frequency smoothing */
			if (min1Occurences[i] > splits1MinFrequenciesSum) /* No need to divide both sides by callStackOccurences */
				min1Occurences[i] = (long) splits1MinFrequenciesSum;

			splits3MinFrequenciesSum = splits3MinFrequenciesCache.get("" + callStackId + "_p" + i);
			model.put("splits3MinFreqSum_" + callStackId + "_p" + i + "_"
					+ (double) min3Occurences[i] / aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[i]),
					"");
			/* Frequency smoothing */
			if (min3Occurences[i] > splits3MinFrequenciesSum)
				min3Occurences[i] = (long) splits3MinFrequenciesSum;
		}

		if (invocation.get("_returns").getAsBoolean()) {
			JsonElement result = invocation.get("result");
			length = result.toString().length();
			model.put("whllens_" + callStackId + "_r_" + length, "");
			wholeLengthMean = wholeLengthsMeansCache.get(callStackId + "_r");
			wholeLengthStdev = wholeLengthsStdevsCache.get(callStackId + "_r");
			wholeLengthVariations[numberOfParams] = Math.abs(length - wholeLengthMean) / wholeLengthStdev;
			minPathOccurences[numberOfParams] = min3Occurences[numberOfParams] = min1Occurences[numberOfParams] = callStackOccurences;
			maxNumberVariations[numberOfParams] = maxStringLengthVariations[numberOfParams] = 0.5;
			build("r", "r", result, numberOfParams);
			buildRecordFromResult(result);

			splits1MinFrequenciesSum = splits1MinFrequenciesCache.get("" + callStackId + "_r");
			model.put(
					"splits1MinFreqSum_" + callStackId + "_r_"
							+ (double) min1Occurences[numberOfParams]
									/ aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[numberOfParams]),
					"");
			/* Frequency smoothing */
			if (min1Occurences[numberOfParams] > splits1MinFrequenciesSum) /*
																			 * No need to divide both sides by
																			 * callStackOccurences
																			 */
				min1Occurences[numberOfParams] = (long) splits1MinFrequenciesSum;

			splits3MinFrequenciesSum = splits3MinFrequenciesCache.get("" + callStackId + "_r");
			model.put(
					"splits3MinFreqSum_" + callStackId + "_r_"
							+ (double) min3Occurences[numberOfParams]
									/ aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[numberOfParams]),
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
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			Iterator<Entry<String, JsonElement>> entries = jsonObject.entrySet().iterator();
			while (entries.hasNext()) {
				String key = (String) entries.next().getKey();
				build(pathToNode.isEmpty() ? key : pathToNode + '_' + key,
						aggregatedPathToNode.isEmpty() ? key : aggregatedPathToNode + '_' + key, jsonObject.get(key),
						paramIndex);
			}
		} else {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			model.put("paths_" + callStackId + "_" + pathToNode, "");
			long pathOccurences = pathsCache.get(callStackId + "_" + pathToNode);
			if (pathOccurences < minPathOccurences[paramIndex])
				minPathOccurences[paramIndex] = pathOccurences;

			String key = "aggpaths_" + callStackId + "_" + aggregatedPathToNode;
			if (!model.containsKey(key))
				model.put(key, "");

			if (primitive.isString()) {
				String value = primitive.getAsString();
				model.put("strlens_" + callStackId + '_' + aggregatedPathToNode + '_' + value.length(), "");
				double stringLengthsMean = stringLengthsMeansCache.get(callStackId + '_' + aggregatedPathToNode);
				double stringLengthsStdev = stringLengthsStdevsCache.get(callStackId + '_' + aggregatedPathToNode);
				double stringLengthVariation = Math.abs(value.length() - stringLengthsMean) / stringLengthsStdev;
				if (stringLengthVariation > 1 && stringLengthVariation > maxStringLengthVariations[paramIndex])
					maxStringLengthVariations[paramIndex] = stringLengthVariation;

				long minSplitOccurences;
				minSplitOccurences = getSplits(value, 1, callStackId, aggregatedPathToNode);
				if (minSplitOccurences < min1Occurences[paramIndex]) {
					min1Occurences[paramIndex] = minSplitOccurences;
					min1AggregatedPathToNode[paramIndex] = aggregatedPathToNode;
				}
				minSplitOccurences = getSplits(value, 3, callStackId, aggregatedPathToNode);
				if (minSplitOccurences < min3Occurences[paramIndex]) {
					min3Occurences[paramIndex] = minSplitOccurences;
					min3AggregatedPathToNode[paramIndex] = aggregatedPathToNode;
				}
			} else if (primitive.isNumber()) {
				double value = primitive.getAsNumber().doubleValue();
				model.put("numbers_" + callStackId + '_' + aggregatedPathToNode + '_' + value, "");
				double numbersMean = numbersMeansCache.get(callStackId + '_' + aggregatedPathToNode);
				double numbersStdev = numbersStdevsCache.get(callStackId + '_' + aggregatedPathToNode);
				double numberVariation = Math.abs(value - numbersMean) / numbersStdev;
				if (numberVariation > 1 && numberVariation > maxNumberVariations[paramIndex])
					maxNumberVariations[paramIndex] = numberVariation;
			}
		}
	}

	private long getSplits(String input, int n, String callStackId, String aggregatedPathToNode) {
		Splitter splitter = Splitter.fixedLength(n);
		long minSplitOccurences = -1;

		for (int i = 0; i < n && i < input.length(); i++) {
			Iterable<String> splits = splitter.split(input.substring(i));

			for (String split : splits) {
				String key = "splits" + n + "_" + callStackId + "_" + aggregatedPathToNode + "_" + split;
				if (!model.containsKey(key))
					model.put(key, "");

				long splitOccurences = 0;
				if (n == 1)
					splitOccurences = splits1Cache.get(callStackId + "_" + aggregatedPathToNode + "_" + split);
				else if (n == 3)
					splitOccurences = splits3Cache.get(callStackId + "_" + aggregatedPathToNode + "_" + split);

				if ((minSplitOccurences >= 0 && splitOccurences < minSplitOccurences) || minSplitOccurences == -1)
					minSplitOccurences = splitOccurences;
			}
		}
		return minSplitOccurences;
	}

	private void buildRecordFromParam(JsonElement pi, int i) {
		if (!pi.isJsonPrimitive()) {
			record.put("p" + i + "_length_variation", wholeLengthVariations[i]);
			record.put("p" + i + "_path_min_f", (double) minPathOccurences[i] / callStackOccurences);
			record.put("p" + i + "_min_if1",
					(double) min1Occurences[i] / aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[i]));
			record.put("p" + i + "_min_if3",
					(double) min3Occurences[i] / aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[i]));
			record.put("p" + i + "_max_string_length_variation", maxStringLengthVariations[i]);
			record.put("p" + i + "_max_number_variation", maxNumberVariations[i]);
		} else if (pi.getAsJsonPrimitive().isString()) {
			record.put("p" + i + "_min_if1",
					(double) min1Occurences[i] / aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[i]));
			record.put("p" + i + "_min_if3",
					(double) min3Occurences[i] / aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[i]));
			record.put("p" + i + "_length_variation", (double) maxStringLengthVariations[i]);
		} else if (pi.getAsJsonPrimitive().isNumber()) {
			record.put("p" + i + "_number_variation", (double) maxNumberVariations[i]);
		}
	}

	private void buildRecordFromResult(JsonElement result) {
		if (invocation.get("_returnsString").getAsBoolean()) {
			record.put("r_min_if1", (double) min1Occurences[numberOfParams]
					/ aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[numberOfParams]));
			record.put("r_min_if3", (double) min3Occurences[numberOfParams]
					/ aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[numberOfParams]));
			record.put("r_length_variation", maxStringLengthVariations[numberOfParams]);
		} else if (result.isJsonPrimitive() && result.getAsJsonPrimitive().isNumber()) {
			record.put("r_number_variation", maxNumberVariations[numberOfParams]);
		} else {
			record.put("r_length_variation", wholeLengthVariations[numberOfParams]);
			record.put("r_path_min_f", (double) minPathOccurences[numberOfParams] / callStackOccurences);
			record.put("r_min_if1", (double) min1Occurences[numberOfParams]
					/ aggPathsCache.get(callStackId + "_" + min1AggregatedPathToNode[numberOfParams]));
			record.put("r_min_if3", (double) min3Occurences[numberOfParams]
					/ aggPathsCache.get(callStackId + "_" + min3AggregatedPathToNode[numberOfParams]));
			record.put("r_max_string_length_variation", maxStringLengthVariations[numberOfParams]);
			record.put("r_max_number_variation", maxNumberVariations[numberOfParams]);
		}
	}
}