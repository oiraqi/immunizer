package org.immunizer.microagents.sensor;

import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.matcher.ElementMatcher;
import static net.bytebuddy.matcher.ElementMatchers.*;

class MethodTransformer implements Transformer {

    Junction<? super MethodDescription> matcher;
    private boolean entry = false;

    public MethodTransformer(Junction<? super MethodDescription> matcher) {
        this(matcher, false);
    }

    public MethodTransformer(Junction<? super MethodDescription> matcher, boolean entry) {
        ElementMatcher<Iterable<? extends ParameterDescription>> parameterMatcher = parameterDescriptions -> {
            return (parameterDescriptions != null && parameterDescriptions.iterator().hasNext());
        };

        this.matcher = matcher.and(isPublic()).and(hasParameters(parameterMatcher));
        this.entry = entry;
    }

    @Override
    public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
            final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {

        return builder.method(matcher).intercept(Advice.to(entry? EntryAdvice.class:MethodAdvice.class));
    }
}
