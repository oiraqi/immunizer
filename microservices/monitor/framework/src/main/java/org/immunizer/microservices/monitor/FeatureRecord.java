package org.immunizer.microservices.monitor;

import java.util.Map;
import java.io.Serializable;

public class FeatureRecord implements Serializable {

    private static final long serialVersionUID = 1354353L;
    private String tag;
    private String label;
    private String swId;            // ISO/IEC 19770-2 SWID
    private String icxId;           // Instance context ID
    private String callStackId;     // Call stack ID
    private String fullyQualifiedMethodName;
    private Map<String, Double> record;
    private Map<String, String> suspects;

    protected FeatureRecord() {
    }

    public FeatureRecord(String callStackId, String tag, String label, String fullyQualifiedMethodName, String swId, String icxId,
            Map<String, Double> record, Map<String, String> suspects) {
        this.tag = tag;
        this.label = label;
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.swId = swId;
        this.icxId = icxId;
        this.callStackId = callStackId;
        this.record = record;
        this.suspects = suspects;
    }

    public String getCallStackId() {
        return callStackId;
    }

    public String getTag() {
        return tag;
    }

    public String getLabel() {
        return label;
    }

    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    public String getSwId() {
        return swId;
    }

    public String getIcxId() {
        return icxId;
    }

    public Map<String, Double> getRecord() {
        return record;
    }

    public Map<String, String> getSuspects() {
        return suspects;
    }
}