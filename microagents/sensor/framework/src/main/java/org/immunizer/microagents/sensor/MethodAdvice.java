package org.immunizer.microagents.sensor;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class MethodAdvice {

    public static InvocationProducer producer = InvocationProducer.getSingleton();

    @Advice.OnMethodEnter
    public static Invocation onEnter(@Advice.Origin String fullyQualifiedMethodName,
            @Advice.AllArguments Object[] params) {
        
        return new Invocation(System.getProperty("swid"), System.getProperty("cxid"), fullyQualifiedMethodName, params);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Enter Invocation invocation,
            @Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result, @Advice.Thrown Throwable thrown) {
        
        if (invocation.returns()) {
            if (thrown != null) {
                if (invocation.returnsNumber())
                    invocation.update(Integer.valueOf("0"), true);
                else
                    invocation.update("NULL", true);
            } else if (result == null)
                invocation.update("NULL", false);
            else
                invocation.update(result, false);
        } else if (thrown != null)
            invocation.update(null, true);
        else
            invocation.update(null, false);

        producer.send(invocation);
    }
}