package org.immunizer.microagents.effector;

public class Alarm {

    private String fullyQualifiedClassName;
    private String name;
    private int callStackId;
    private byte[][] signature;

    public Alarm (String fullyQualifiedClassName, String name, int callStackId, 
                    byte[][] signature) {
        this.fullyQualifiedClassName = fullyQualifiedClassName;
        this.name = name;
        this.callStackId = callStackId;
        this.signature = signature;
    }

    public String getFullyQualifiedClassName() {
        return fullyQualifiedClassName;
    }

    public String getName() {
        return name;
    }

    public int getCallStackId() {
        return callStackId;
    }

    public byte[][] getSignature() {
        return signature;
    }    
}