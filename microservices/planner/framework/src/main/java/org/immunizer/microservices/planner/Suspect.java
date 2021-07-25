package org.immunizer.microservices.planner;

import java.util.Map;
import java.io.Serializable;

public class Suspect implements Serializable {

    private static final long serialVersionUID = 13763933L;
    private String swId;            // ISO/IEC 19770-2 SWID
    private String callStackId;     // Call stack ID
    private String fullyQualifiedMethodName;
    private int paramIndex;
    private int featureIndex;       // 1 - 5
    private String path;
    private String chars;

    protected Suspect() {
    }

    public Suspect(String swId, String callStackId, String fullyQualifiedMethodName, 
        int paramIndex, int featureIndex, String path, String chars) {        
        this.swId = swId;
        this.callStackId = callStackId;
        this.fullyQualifiedMethodName = fullyQualifiedMethodName;
        this.paramIndex = paramIndex;
        this.featureIndex = featureIndex;
        this.path = path;
        this.chars = chars;        
    }

    public Suspect(String swId, String callStackId, String fullyQualifiedMethodName, 
        int paramIndex, int featureIndex, String path) {        
        this(swId, callStackId, fullyQualifiedMethodName, paramIndex, featureIndex, path, null);
    }

    public Suspect(String swId, String callStackId, String fullyQualifiedMethodName, 
        int paramIndex, int featureIndex) {        
        this(swId, callStackId, fullyQualifiedMethodName, paramIndex, featureIndex, null, null);
    }

    public String getCallStackId() {
        return callStackId;
    }

    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    public String getSwId() {
        return swId;
    }

    public int getParameterIndex() {
        return paramIndex;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    public String getPath() {
        return path;
    }

    public String getChars() {
        return chars;
    }
    
}