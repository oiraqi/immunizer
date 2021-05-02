package org.immunizer.microservices.analyzer;

import java.util.Map;
import java.io.Serializable;

public class FeatureRecord implements Serializable {

    private static final long serialVersionUID = 1354353L;

    private String callStackId;
    private String threadTag;
    private long startTime;
    private long endTime;
    private String swid;
    private String fullyQualifiedMethodName;
    private Map<String, Double> record;
    private String parameters;
    private String result;

    protected FeatureRecord() {
    }

    public FeatureRecord(String callStackId, String threadTag, long startTime, long endTime,
            String fullyQualifiedMethodName, String swid, Map<String, Double> record, String parameters,
            String result) {
        this.callStackId = callStackId;
        this.threadTag = threadTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.swid = swid;
        this.record = record;
        this.parameters = parameters;
        this.result = result;
    }

    public String getCallStackId() {
        return callStackId;
    }

    public String getThreadTag() {
        return threadTag;
    }

    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    public String getSwid() {
        return swid;
    }

    public Map<String, Double> getRecord() {
        return record;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}