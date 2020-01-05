package org.immunizer.acquisition;

public class KafkaSensor extends Sensor {

    public void stream(Invocation invocation) {
        System.out.println("SSSSSSSSSSSSS " + invocation.getNumberOfParams() + " SSSSSSSSSSSSSSSSSS");
    }
}