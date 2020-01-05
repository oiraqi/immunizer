package org.immunizer.acquisition.sensor;

import org.immunizer.acquisition.Invocation;

public class KafkaSensor extends Sensor {

    public void stream(Invocation invocation) {
        System.out.println("++++++++++ KAFKA " + invocation.getNumberOfParams() + " KAFKA ++++++++++");
    }
}