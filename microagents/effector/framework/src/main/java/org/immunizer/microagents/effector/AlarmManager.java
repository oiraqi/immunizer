package org.immunizer.microagents.effector;

import java.util.HashMap;
import java.util.Vector;

public class AlarmManager {

    private HashMap<Integer, Vector<byte[][]>> alarms;
    private static AlarmManager singleton;

    private AlarmManager() {
        alarms = new HashMap<Integer, Vector<byte[][]>>();
    }

    public static AlarmManager getSingleton() {
        if (singleton == null) {
            singleton = new AlarmManager();
        }
        return singleton;
    }
    
    public void addAlarm(Alarm alarm) {
        Vector<byte[][]> signatures;
        if (!alarms.containsKey(alarm.getCallStackId())) {
            signatures = new Vector<byte[][]>();
        } else {
            signatures = alarms.get(alarm.getCallStackId());
        }
        signatures.add(alarm.getSignature());
        alarms.put(alarm.getCallStackId(), signatures);
    }

    public boolean isProtected(int callStackId) {
        return alarms.containsKey(callStackId);
    }

    public boolean isAttack(String callStackId, Object[] params) {
        // Vector<byte[][]> signatures = alarms.get(callStackId);
        return false;
    }
}