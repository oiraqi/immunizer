package org.immunizer.instrumentation.misc;

import java.util.HashMap;

import org.immunizer.acquisition.consumer.FeatureRecord;

/**
 * A FeatureRecord decorator/wrapper to organize models in
 * folders according to OWASP Benchmark test cases
 * A folder per test case
 */
public class BenchmarkFeatureRecord extends FeatureRecord{

    private FeatureRecord featureRecord;

    public BenchmarkFeatureRecord(FeatureRecord featureRecord){
        this.featureRecord = featureRecord;
    }

    public int getCallStackId(){
        return featureRecord.getCallStackId();
    }

    /*public StackTraceElement[] getCallStack(){
        return featureRecord.getCallStack();
    }*/

    public String getThreadTag(){
        return featureRecord.getThreadTag();
    }

    public String getFullyQualifiedMethodName(){
        return featureRecord.getFullyQualifiedMethodName();
    }

    public String getVersion(){
        return featureRecord.getVersion();
    }

    public HashMap<String, Double> getRecord(){
        return featureRecord.getRecord();
    }
    
    public long getStartTime(){
        return featureRecord.getStartTime();
    }

    public long getEndTime(){
        return featureRecord.getEndTime();
    }

    public String getValues(){
        return featureRecord.getValues();
    }

    /**
     * A wrapper method to organize models in folders
     * according to OWASP Benchmark test cases
     * A folder per test case 
     * @return the enhanced model path
     */
    public String getModel(){
        /*StackTraceElement[] callStack = featureRecord.getCallStack();
        for(StackTraceElement ste : callStack)
            if(ste.getClassName().startsWith("org.owasp.benchmark.testcode."))
                return ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1) 
                    + "/" + featureRecord.getModel();*/
        
        return featureRecord.getModel();
    }
}