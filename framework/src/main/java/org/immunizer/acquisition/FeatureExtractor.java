package org.immunizer.acquisition;

import java.util.Iterator;
import java.util.HashMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.immunizer.acquisition.Invocation;

import com.google.gson.JsonElement;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import com.google.common.base.Splitter;

public class FeatureExtractor {

	private HashMap<String, Integer> dictionary, callStackOccurences, pathToNodeOccurences,
			aggregatedPathToNodeOccurences, flushCounters, csCounters1, csCounters3;
	private HashMap<String, HashMap<String, Double>> cmqs;
	private Gson gson;
	private HashMap<String, PrintWriter> models;
	private static FeatureExtractor singleton;
	private HashMap<String, Double> sumMinIF1s, sumMinIF3s;
	private String modelsRepository;
	private int bufferSize;
	private int skipFirstRecords;	// Use these first records just to learn. Don't log them.  

	private FeatureExtractor() {
		dictionary = new HashMap<String, Integer>();
		callStackOccurences = new HashMap<String, Integer>();
		pathToNodeOccurences = new HashMap<String, Integer>();
		cmqs = new HashMap<String, HashMap<String, Double>>();
		aggregatedPathToNodeOccurences = new HashMap<String, Integer>();
		flushCounters = new HashMap<String, Integer>();
		csCounters1 = new HashMap<String, Integer>();
		csCounters3 = new HashMap<String, Integer>();
		gson = new Gson();
		models = new HashMap<String, PrintWriter>();
		sumMinIF1s = new HashMap<String, Double>();
		sumMinIF3s = new HashMap<String, Double>();
		modelsRepository = System.getProperty("models");
		bufferSize = Integer.parseInt(System.getProperty("buffer"));
		skipFirstRecords = Integer.parseInt(System.getProperty("skip"));
		File path = new File(modelsRepository);
		if (!path.exists())
			path.mkdir();
	}

	public static FeatureExtractor getSingleton() {
		if (singleton == null)
			singleton = new FeatureExtractor();

		return singleton;
	}

	/**
	 * Extracts features from invocation Uses build method to build features
	 * recursively for each parameter tree or returned value tree
	 * 
	 * @param invocation
	 * @return The Feature Record
	 */
	public FeatureRecord extract(Invocation invocation) {
		if (LazySerializationHelper.skipInvocation(invocation))
			return null;
		
		HashMap<String, Double> record = new HashMap<String, Double>();
		int[] lengths;
		double[] minIF1s, minIF3s, pathToNodeMinFreqs, maxNumberVariations, maxStringLengthVariations;

		boolean exception = invocation.getException();
		// long executionTime = invocation.getExecutionTime();
		int callStackId = invocation.getCallStackId();
		int numberOfParams = invocation.getNumberOfParams();
		JsonElement parameters = null, result = null;
		synchronized (callStackOccurences) {
			if (!callStackOccurences.containsKey("" + callStackId))
				callStackOccurences.put("" + callStackId, 1);
			else
				callStackOccurences.put("" + callStackId, callStackOccurences.get("" + callStackId) + 1);
		}
		if (numberOfParams > 0 && invocation.returns()) {
			try {
				parameters = gson.toJsonTree(invocation.getParams());
				result = gson.toJsonTree(invocation.getResult());
			} catch (Throwable th) {
				/**
				 * There are some complex types that Gson does not know how to deal with.
				 * They require writing custom adapters, and registring them with Gson.
				 * Otherwise, Gson.toJsonTree throws an error. To avoid a program crash and
				 * identify these types, we catch errors, return back the counter to its
				 * previous value and return null, as if such an invocation never occured.
				 * Finally, we log errors for analysis and eventually writing the
				 * necessary adapters.
				 */
				synchronized (callStackOccurences) {
					callStackOccurences.put("" + callStackId, callStackOccurences.get("" + callStackId) - 1);
				}
				//System.out.println(invocation.getFullyQualifiedMethodName() + ": " + th.getMessage());
				return null;
			}
			lengths = new int[numberOfParams + 1];
			minIF1s = new double[numberOfParams + 1];
			minIF3s = new double[numberOfParams + 1];
			pathToNodeMinFreqs = new double[numberOfParams + 1];
			maxNumberVariations = new double[numberOfParams + 1];
			maxStringLengthVariations = new double[numberOfParams + 1];
			for (int i = 0; i < numberOfParams; i++) {
				lengths[i] = parameters.getAsJsonArray().get(i).toString().length();
				minIF1s[i] = 1;
				minIF3s[i] = 1;
				pathToNodeMinFreqs[i] = 1;
				maxNumberVariations[i] = 0;
				maxStringLengthVariations[i] = 0;
			}
			lengths[numberOfParams] = result.toString().length();
			minIF1s[numberOfParams] = 1;
			minIF3s[numberOfParams] = 1;
			pathToNodeMinFreqs[numberOfParams] = 1;
			maxNumberVariations[numberOfParams] = 0;
			maxStringLengthVariations[numberOfParams] = 0;
			build(callStackId, "p", "p", parameters, -1, false, numberOfParams, minIF1s, minIF3s, pathToNodeMinFreqs,
					maxNumberVariations, maxStringLengthVariations);
			build(callStackId, "r", "r", result, -1, false, numberOfParams, minIF1s, minIF3s, pathToNodeMinFreqs,
					maxNumberVariations, maxStringLengthVariations);
			for (int i = 0; i < numberOfParams; i++) {
				JsonElement pi = parameters.getAsJsonArray().get(i);
				if (!pi.isJsonPrimitive()) {
					record.put("p_" + i + "_length_variation",
							getVariation("" + callStackId + "_p_" + i + "_length", (double) lengths[i]));
					record.put("p_" + i + "_path_min_f", pathToNodeMinFreqs[i]);
					record.put("p_" + i + "_min_if1", getSmoothedMinIF1("" + callStackId + "_p_" + i, minIF1s[i]));
					record.put("p_" + i + "_min_if3", getSmoothedMinIF3("" + callStackId + "_p_" + i, minIF3s[i]));
					record.put("p_" + i + "_max_string_length_variation", maxStringLengthVariations[i]);
					record.put("p_" + i + "_max_number_variation", maxNumberVariations[i]);
				} else if (pi.getAsJsonPrimitive().isString()) {
					record.put("p_" + i + "_min_if1", getSmoothedMinIF1("" + callStackId + "_p_" + i, minIF1s[i]));
					record.put("p_" + i + "_min_if3", getSmoothedMinIF3("" + callStackId + "_p_" + i, minIF3s[i]));
					record.put("p_" + i + "_length_variation",
							getVariation("" + callStackId + "_p_" + i + "_length", (double) lengths[i]));
				} else if (pi.getAsJsonPrimitive().isNumber())
					record.put("p_" + i + "_number_variation", maxNumberVariations[i]);
			}
			if (invocation.returnsString()) {
				record.put("r_min_if1", getSmoothedMinIF1("" + callStackId + "_r", minIF1s[numberOfParams]));
				record.put("r_min_if3", getSmoothedMinIF3("" + callStackId + "_r", minIF3s[numberOfParams]));
				record.put("r_length_variation",
						getVariation("" + callStackId + "_r_length", (double) lengths[numberOfParams]));
			} else if (invocation.returnsNumber()) {
				record.put("r_number_variation", maxNumberVariations[numberOfParams]);
			} else {
				record.put("r_length_variation",
						getVariation("" + callStackId + "_r_length", (double) lengths[numberOfParams]));
				record.put("r_path_min_f", pathToNodeMinFreqs[numberOfParams]);
				record.put("r_min_if1", getSmoothedMinIF1("" + callStackId + "_r", minIF1s[numberOfParams]));
				record.put("r_min_if3", getSmoothedMinIF3("" + callStackId + "_r", minIF3s[numberOfParams]));
				record.put("r_max_string_length_variation", maxStringLengthVariations[numberOfParams]);
				record.put("r_max_number_variation", maxNumberVariations[numberOfParams]);
			}
		} else if (numberOfParams > 0) {
			try {
				parameters = gson.toJsonTree(invocation.getParams());
			} catch (Throwable th) {
				/**
				 * There are some complex types that Gson does not know how to deal with.
				 * They require writing custom adapters, and registring them with Gson.
				 * Otherwise, Gson.toJsonTree throws an error. To avoid a program crash and
				 * identify these types, we catch errors, return back the counter to its
				 * previous value and return null, as if such an invocation never occured.
				 * Finally, we log errors for analysis and eventually writing the
				 * necessary adapters.
				 */
				synchronized (callStackOccurences) {
					callStackOccurences.put("" + callStackId, callStackOccurences.get("" + callStackId) - 1);
				}
				//System.out.println(invocation.getFullyQualifiedMethodName() + ": " + th.getMessage());
				return null;
			}
			lengths = new int[numberOfParams];
			minIF1s = new double[numberOfParams];
			minIF3s = new double[numberOfParams];
			pathToNodeMinFreqs = new double[numberOfParams];
			maxNumberVariations = new double[numberOfParams];
			maxStringLengthVariations = new double[numberOfParams];
			for (int i = 0; i < numberOfParams; i++) {
				lengths[i] = parameters.getAsJsonArray().get(i).toString().length();
				minIF1s[i] = 1;
				minIF3s[i] = 1;
				pathToNodeMinFreqs[i] = 1;
				maxNumberVariations[i] = 0;
				maxStringLengthVariations[i] = 0;
			}
			build(callStackId, "p", "p", parameters, -1, false, numberOfParams, minIF1s, minIF3s, pathToNodeMinFreqs,
					maxNumberVariations, maxStringLengthVariations);
			for (int i = 0; i < numberOfParams; i++) {
				JsonElement pi = parameters.getAsJsonArray().get(i);
				if (!pi.isJsonPrimitive()) {
					record.put("p_" + i + "_length_variation",
							getVariation("" + callStackId + "_p_" + i + "_length", (double) lengths[i]));
					record.put("p_" + i + "_path_min_f", pathToNodeMinFreqs[i]);
					record.put("p_" + i + "_min_if1", getSmoothedMinIF1("" + callStackId + "_p_" + i, minIF1s[i]));
					record.put("p_" + i + "_min_if3", getSmoothedMinIF3("" + callStackId + "_p_" + i, minIF3s[i]));
					record.put("p_" + i + "_max_string_length_variation", maxStringLengthVariations[i]);
					record.put("p_" + i + "_max_number_variation", maxNumberVariations[i]);
				} else if (pi.getAsJsonPrimitive().isString()) {
					record.put("p_" + i + "_min_if1", getSmoothedMinIF1("" + callStackId + "_p_" + i, minIF1s[i]));
					record.put("p_" + i + "_min_if3", getSmoothedMinIF3("" + callStackId + "_p_" + i, minIF3s[i]));
					record.put("p_" + i + "_length_variation",
							getVariation("" + callStackId + "_p_" + i + "_length", (double) lengths[i]));
				} else if (pi.getAsJsonPrimitive().isNumber()) {
					record.put("p_" + i + "_number_variation", maxNumberVariations[i]);
				}
			}
		} else if (invocation.returns()) {
			try {
				result = gson.toJsonTree(invocation.getResult());
			} catch (Throwable th) {
				/**
				 * There are some complex types that Gson does not know how to deal with.
				 * They require writing custom adapters, and registring them with Gson.
				 * Otherwise, Gson.toJsonTree throws an error. To avoid a program crash and
				 * identify these types, we catch errors, return back the counter to its
				 * previous value and return null, as if such an invocation never occured.
				 * Finally, we log errors for analysis and eventually writing the
				 * necessary adapters.
				 */
				synchronized (callStackOccurences) {
					callStackOccurences.put("" + callStackId, callStackOccurences.get("" + callStackId) - 1);
				}
				//System.out.println(invocation.getFullyQualifiedMethodName() + ": " + th.getMessage());
				return null;
			}
			lengths = new int[1];
			minIF1s = new double[1];
			minIF3s = new double[1];
			pathToNodeMinFreqs = new double[1];
			maxNumberVariations = new double[1];
			maxStringLengthVariations = new double[1];
			lengths[0] = result.toString().length();
			minIF1s[0] = 1;
			minIF3s[0] = 1;
			pathToNodeMinFreqs[0] = 1;
			maxNumberVariations[0] = 0;
			maxStringLengthVariations[0] = 0;
			build(callStackId, "r", "r", result, -1, false, 0, minIF1s, minIF3s, pathToNodeMinFreqs,
					maxNumberVariations, maxStringLengthVariations);
			if (invocation.returnsString()) {
				record.put("r_min_if1", getSmoothedMinIF1("" + callStackId + "_r", minIF1s[0]));
				record.put("r_min_if3", getSmoothedMinIF3("" + callStackId + "_r", minIF3s[0]));
				record.put("r_length_variation", getVariation("" + callStackId + "_r_length", (double) lengths[0]));
			} else if (result.isJsonPrimitive() && result.getAsJsonPrimitive().isNumber()) {
				record.put("r_number_variation", maxNumberVariations[0]);
			} else {
				record.put("r_length_variation", getVariation("" + callStackId + "_r_length", (double) lengths[0]));
				record.put("r_path_min_f", pathToNodeMinFreqs[0]);
				record.put("r_min_if1", getSmoothedMinIF1("" + callStackId + "_r", minIF1s[0]));
				record.put("r_min_if3", getSmoothedMinIF3("" + callStackId + "_r", minIF3s[0]));
				record.put("r_max_string_length_variation", maxStringLengthVariations[0]);
				record.put("r_max_number_variation", maxNumberVariations[0]);
			}
		}
		// record.put("execution_time", (double)executionTime);
		record.put("exception", exception ? (double) 1 : 0);
		return new FeatureRecord(callStackId, /*invocation.getCallStack(), */invocation.getThreadTag(),
				invocation.getStartTime(), invocation.getEndTime(), invocation.getFullyQualifiedMethodName(),
				invocation.getVersion(), record,
				(parameters != null) ? parameters.toString().replace('"', '_').replace('\'', '_').replace(' ', '_')
						: null,
				(result != null) ? result.toString().replace('"', '_').replace('\'', '_').replace(' ', '_') : null);
	}

	public synchronized void log(FeatureRecord featureRecord) {
		if (callStackOccurences.get("" + featureRecord.getCallStackId()) <= skipFirstRecords)
			return;

		try {
			String model = featureRecord.getModel();
			if (!models.containsKey(model)) {
				if (model.indexOf('/') > 0)
					new File(modelsRepository + '/' + model.substring(0, model.indexOf('/'))).mkdir();
				models.put(model, new PrintWriter(new FileWriter(modelsRepository + '/' + model + ".log")));
				flushCounters.put(model, 0);
			}
			models.get(model).println(featureRecord.getValues());
			flushCounters.put(model, flushCounters.get(model) + 1);
			if (flushCounters.get(model) >= bufferSize) {	// Typical bufferSize values: [100 - 500]
				models.get(model).flush();
				flushCounters.put(model, 0);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Builds features for each parameter or returned value by walking recursively
	 * through the parameter tree or returned value tree
	 * 
	 * @param callStackId
	 * @param pathToNode
	 * @param aggregatedPathToNode      for sibling/relative grouping and
	 *                                  comparision
	 * @param jsonElement
	 * @param paramIndex
	 * @param isParentAnArray
	 * @param numberOfParams
	 * @param minIF1s
	 * @param minIF3s
	 * @param pathToNodeMinFreqs
	 * @param maxNumberVariations
	 * @param maxStringLengthVariations
	 */
	private void build(int callStackId, String pathToNode, String aggregatedPathToNode, JsonElement jsonElement,
			int paramIndex, boolean isParentAnArray, int numberOfParams, double[] minIF1s, double[] minIF3s,
			double[] pathToNodeMinFreqs, double[] maxNumberVariations, double[] maxStringLengthVariations) {
		String _key = "" + callStackId + "_" + aggregatedPathToNode;
		synchronized (aggregatedPathToNodeOccurences) {
			if (!aggregatedPathToNodeOccurences.containsKey(_key))
				aggregatedPathToNodeOccurences.put(_key, 1);
			else
				aggregatedPathToNodeOccurences.put(_key, (aggregatedPathToNodeOccurences.get(_key) + 1));
		}

		if (jsonElement.isJsonNull())
			return;

		if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			if (pathToNode.equals("p")) {
				for (int i = 0; i < numberOfParams; i++) {
					build(callStackId, "p" + '_' + i, "p" + '_' + i, jsonArray.get(i), i, false, numberOfParams,
							minIF1s, minIF3s, pathToNodeMinFreqs, maxNumberVariations, maxStringLengthVariations);
				}
			} else
				for (int i = 0; i < jsonArray.size(); i++)
					/**
					 * While we call build for each element of the array with a different pathToNode
					 * (second parameter: pathToNode + '_' + i), we keep track of the same
					 * aggregatedPathToNode for all of them (third parameter) to group and compare
					 * siblings and relatives
					 */
					build(callStackId, pathToNode + '_' + i, aggregatedPathToNode, jsonArray.get(i), paramIndex, true,
							numberOfParams, minIF1s, minIF3s, pathToNodeMinFreqs, maxNumberVariations,
							maxStringLengthVariations);
		} else if (jsonElement.isJsonObject()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Iterator<String> keys = jsonObject.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				build(callStackId, pathToNode.isEmpty() ? key : pathToNode + '_' + key,
						aggregatedPathToNode.isEmpty() ? key : aggregatedPathToNode + '_' + key, jsonObject.get(key),
						paramIndex, isParentAnArray, numberOfParams, minIF1s, minIF3s, pathToNodeMinFreqs,
						maxNumberVariations, maxStringLengthVariations);
			}
		} else {
			JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
			String __key = "" + callStackId + "_" + pathToNode;
			int occurences = 0;
			synchronized (pathToNodeOccurences) {
				if (!pathToNodeOccurences.containsKey(__key))
					occurences = 1;
				else
					occurences = pathToNodeOccurences.get(__key) + 1;
				pathToNodeOccurences.put(__key, occurences);
			}
			double freq = (double) occurences / callStackOccurences.get("" + callStackId);
			if (paramIndex >= 0) {
				if (freq < pathToNodeMinFreqs[paramIndex])
					pathToNodeMinFreqs[paramIndex] = freq;
			} else if (freq < pathToNodeMinFreqs[numberOfParams])
				pathToNodeMinFreqs[numberOfParams] = freq;
			if (primitive.isString()) {
				String value = primitive.getAsString();
				double minIF1 = getMinIF(value, 1, _key, aggregatedPathToNodeOccurences.get(_key));
				double minIF3 = getMinIF(value, 3, _key, aggregatedPathToNodeOccurences.get(_key));
				if (paramIndex >= 0) {
					if (minIF1 < minIF1s[paramIndex])
						minIF1s[paramIndex] = minIF1;
					if (minIF3 < minIF3s[paramIndex])
						minIF3s[paramIndex] = minIF3;
				} else {
					if (minIF1 < minIF1s[numberOfParams])
						minIF1s[numberOfParams] = minIF1;
					if (minIF3 < minIF3s[numberOfParams])
						minIF3s[numberOfParams] = minIF3;
				}
				double variation = getVariation("" + callStackId + "_" + aggregatedPathToNode, value.length());
				if (paramIndex >= 0) {
					if (variation > maxStringLengthVariations[paramIndex])
						maxStringLengthVariations[paramIndex] = variation;
				} else if (variation > maxStringLengthVariations[numberOfParams])
					maxStringLengthVariations[numberOfParams] = variation;
			} else if (primitive.isNumber()) {
				double value = primitive.getAsNumber().doubleValue();
				double variation = getVariation("" + callStackId + "_" + aggregatedPathToNode, value);
				if (paramIndex >= 0) {
					if (variation > maxNumberVariations[paramIndex])
						maxNumberVariations[paramIndex] = variation;
				} else if (variation > maxNumberVariations[numberOfParams])
					maxNumberVariations[numberOfParams] = variation;
			}
		}
	}

	private synchronized double getVariation(String key, double value) {
		HashMap<String, Double> cmq;
		double c, m, mb, q, sd;
		if (!cmqs.containsKey(key)) {
			cmq = new HashMap<String, Double>();
			c = 0;
			m = value;
			q = 0;
			cmqs.put(key, cmq);
		} else {
			cmq = cmqs.get(key);
			c = cmq.get("c");
			m = cmq.get("m");
			q = cmq.get("q");
		}
		c++;
		mb = m;
		m += (value - m) / c;
		q += (value - mb) * (value - m);
		cmq.put("m", m);
		cmq.put("c", c);
		cmq.put("q", q);
		sd = Math.sqrt(q / c);

		/**
		 * Variation Smoothing Whenever a number or a string length deviates from the
		 * mean value across invocations by less than the standard deviation, we set its
		 * variation to 0.5
		 **/
		if (sd == 0 || Math.abs(value - m) < sd)
			return 0.5;

		return Math.abs(value - m) / sd;
	}

	private synchronized double getMinIF(String input, int n, String key, int counter) {
		HashMap<String, String> splitsSeen = new HashMap<String, String>();
		Splitter splitter = Splitter.fixedLength(n);
		int minOccurences = counter;

		for (int i = 0; i < n && i < input.length(); i++) {
			Iterable<String> splits = splitter.split(input.substring(i));

			for (String split : splits) {
				if (splitsSeen.containsKey(split))
					continue;
				if (!dictionary.containsKey(key + '_' + split)) {
					dictionary.put(key + '_' + split, 1);
					minOccurences = 1;
				} else {
					int occurences = dictionary.get(key + '_' + split).intValue() + 1;
					dictionary.put(key + '_' + split, occurences);
					if (occurences < minOccurences)
						minOccurences = occurences;
				}
				splitsSeen.put(split, "");
			}
		}
		return (double) minOccurences / counter;
	}

	/**
	 * Applies smoothing if need be to min frequency of 3-grams
	 * 
	 * @param key
	 * @param minIF3
	 * @return
	 */
	private synchronized double getSmoothedMinIF3(String key, double minIF3) {
		if (!sumMinIF3s.containsKey(key)) {
			sumMinIF3s.put(key, minIF3);
			csCounters3.put(key, 1);
			return minIF3;
		}
		double sum = sumMinIF3s.get(key).doubleValue();
		int count = csCounters3.get(key).intValue();
		sumMinIF3s.put(key, sum + minIF3);
		csCounters3.put(key, count + 1);

		/**
		 * Frequency Smoothing Whenever a minimum frequency is higher than the average
		 * minimum frequency across invocations, we set it to the average minimum
		 * frequency
		 */
		if (minIF3 > sum / count)
			return sum / count;

		// Return actual value otherwise
		return minIF3;
	}

	/**
	 * Applies smoothing if need be to min frequency of 1-grams
	 * 
	 * @param key
	 * @param minIF1
	 * @return
	 */
	private synchronized double getSmoothedMinIF1(String key, double minIF1) {
		if (!sumMinIF1s.containsKey(key)) {
			sumMinIF1s.put(key, minIF1);
			csCounters1.put(key, 1);
			return minIF1;
		}
		double sum = sumMinIF1s.get(key).doubleValue();
		int count = csCounters1.get(key).intValue();
		sumMinIF1s.put(key, sum + minIF1);
		csCounters1.put(key, count + 1);

		/**
		 * Frequency Smoothing Whenever a minimum frequency is higher than the average
		 * minimum frequency across invocations, we set it to the average minimum
		 * frequency
		 */
		if (minIF1 > sum / count)
			return sum / count;

		// Return actual value otherwise
		return minIF1;
	}
}