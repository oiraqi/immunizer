package org.immunizer.microservices.analyzer;

import java.util.HashMap;
import java.util.Iterator;
import java.io.Serializable;

public class FeatureRecord implements Serializable {

    private static final long serialVersionUID = 1354353L;

    private String callStackId;
    // private StackTraceElement[] callStack;
    private String threadTag;
    private long startTime;
    private long endTime;
    private String swid;
    private String fullyQualifiedMethodName;
    private HashMap<String, Double> record;
    private String parameters;
    private String result;

    protected FeatureRecord() {
    }

    public FeatureRecord(String callStackId, /*StackTraceElement[] callStack, */String threadTag, long startTime, long endTime,
            String fullyQualifiedMethodName, String swid, HashMap<String, Double> record, String parameters,
            String result) {
        this.callStackId = callStackId;
        // this.callStack = callStack;
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

    /*public StackTraceElement[] getCallStack() {
        return callStack;
    }*/

    public String getThreadTag() {
        return threadTag;
    }

    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    public String getSwid() {
        return swid;
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