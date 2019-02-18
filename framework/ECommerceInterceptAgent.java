import java.lang.instrument.Instrumentation;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import static net.bytebuddy.matcher.ElementMatchers.*;
import java.util.Random;

public class ECommerceInterceptAgent {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Instrumenter launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."))
				.type(named("Checkout"))
				.transform(new InterceptTransformer()).installOn(inst);
	}

	private static class InterceptTransformer implements Transformer {
		@Override
		public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
				final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {

			return builder.method(isPublic().and(nameStartsWith("process")))
					.intercept(Advice.to(MethodAdvice.class));
		}
	}

	public static class MethodAdvice {

		public static FeatureExtractor featureExtractorSingleton = FeatureExtractor.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(@Advice.This Object object, @Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			long threadTag = Math.abs(new Random().nextLong());
			Thread currentThread = Thread.currentThread();
			int index = currentThread.getName().indexOf("#");
			if (index > 0) {
				String threadBasicName = currentThread.getName().substring(0, index + 1);
				currentThread.setName(threadBasicName + threadTag);
			} else
				currentThread.setName(currentThread.getName() + "#" + threadTag);

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result) {
			// System.out.println("Intercepted");
			if (invocation.returns()) {
				if (result == null)
					invocation.update("NULL", false);
				else
					invocation.update(result, false);
			} else
				invocation.update(null, false);
			FeatureRecord featureRecord = featureExtractorSingleton.extract(invocation);
			if (featureRecord != null)
				featureExtractorSingleton.log(featureRecord);
		}
	}
}
