package org.immunizer.acquisition;

import java.util.HashMap;
import java.util.Iterator;

public class FeatureRecord {
    private int callStackId;
    // private StackTraceElement[] callStack;
    private String threadTag;
    private long startTime;
    private long endTime;
    private String version;
    private String fullyQualifiedMethodName;
    private HashMap<String, Double> record;
    private String parameters;
    private String result;

    protected FeatureRecord() {
    }

    public FeatureRecord(int callStackId, /*StackTraceElement[] callStack, */String threadTag, long startTime, long endTime,
            String fullyQualifiedMethodName, String version, HashMap<String, Double> record, String parameters,
            String result) {
        this.callStackId = callStackId;
        // this.callStack = callStack;
        this.threadTag = threadTag;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.version = version;
        this.record = record;
        this.parameters = parameters;
        this.result = result;
    }

    public int getCallStackId() {
        return callStackId;
    }

    /*public StackTraceElement[] getCallStack() {
        return callStack;
    }*/

    public String getThreadTag() {
        return threadTag;
    }

    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    public String getVersion() {
        return version;
    }

    public HashMap<String, Double> getRecord() {
        return record;
    }

    public String getModel() {
        String method = fullyQualifiedMethodName
                .substring(0, fullyQualifiedMethodName.indexOf(')') + 1).trim();
        
        method = method.replace(' ', '_');
        return method + '_' + callStackId;
    }

    public String getValues() {
        Iterator<Double> values = record.values().iterator();
        StringBuffer buffer = new StringBuffer();
        while (values.hasNext())
            buffer.append(values.next() + " ");
        buffer.append("Thread_" + threadTag);
        return buffer.toString();
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