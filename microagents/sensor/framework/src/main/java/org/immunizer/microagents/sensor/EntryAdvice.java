package org.immunizer.microagents.sensor;

import java.util.Random;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class EntryAdvice {

    @Advice.OnMethodEnter
    public static Invocation onEnter(
            /* @Advice.This Object object, */@Advice.Origin String fullyQualifiedMethodName,
            @Advice.AllArguments Object[] params) {

        String label = "Genuine";
        try {
            String userAgent = (String) params[0].getClass().getMethod("getHeader", java.lang.String.class)
                    .invoke(params[0], "User-Agent");
            if (userAgent == null || !userAgent.equals("JMeter")) {
                label = "Malicious"; /**
                                     * Label it as true positive, just for automatic evaluation of intrusion/oulier
                                     * detection results
                                     */
            }
        } catch (Exception ex) {
        }
        long tag = Math.abs(new Random().nextLong());
        Context.set("tag", "" + tag);
        Context.set("label", label);
        
        System.out.println("TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG");
        System.out.println(Context.get("tag"));
        System.out.println("TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG-TAG");

        System.out.println("LABEL-LABEL-LABEL-LABEL-LABEL-LABEL-LABEL");
        System.out.println(Context.get("label"));
        System.out.println("LABEL-LABEL-LABEL-LABEL-LABEL-LABEL-LABEL");

        return new Invocation(System.getProperty("swid"), System.getProperty("cxid"), fullyQualifiedMethodName, params);
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Enter Invocation invocation,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result) {
        // Do nothing here
    }
}
