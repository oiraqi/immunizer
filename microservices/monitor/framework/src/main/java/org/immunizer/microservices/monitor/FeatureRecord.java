package org.immunizer.microservices.monitor;

import java.util.Map;
import java.io.Serializable;

public class FeatureRecord implements Serializable {

    private static final long serialVersionUID = 1354353L;
    private String callStackId;
    private String tag;
    private String label;
    private String swid;
    private String cxid;
    private String fullyQualifiedMethodName;
    private Map<String, Double> record;

    protected FeatureRecord() {
    }

    public FeatureRecord(String callStackId, String tag, String label, String fullyQualifiedMethodName, String swid, String cxid,
            Map<String, Double> record) {
        this.callStackId = callStackId;
        this.tag = tag;
        this.label = label;
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.swid = swid;
        this.cxid = cxid;
        this.record = record;
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

    public String getSwid() {
        return swid;
    }

    public String getCxid() {
        return cxid;
    }

    public Map<String, Double> getRecord() {
        return record;
    }

}