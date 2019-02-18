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

public class OFBizInterceptAgent {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Instrumenter launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."))
				.type(nameStartsWith("org.apache.ofbiz.accounting.")
						//.or(nameStartsWith("org.apache.ofbiz.manufacturing."))
					)
				.transform(new InterceptTransformer()).installOn(inst);
	}

	private static class InterceptTransformer implements Transformer {
		@Override
		public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
				final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {

			return builder.method(isPublic())
					.intercept(Advice.to(ModelViewMethodAdvice.class))
					/*.method(isPublic().and(nameStartsWith("execute").or(named("prepareCall"))
							.or(named("prepareStatement")).or(nameStartsWith("print")).or(nameStartsWith("write"))))
					.intercept(Advice.to(ModelViewMethodAdvice.class))*/;
		}
	}

	public static class ControllerMethodAdvice {

		public static FeatureExtractor featureExtractorSingleton = FeatureExtractor.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(@Advice.This Object object, @Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			System.out.println(fullyQualifiedMethodName);

			/*if (fullyQualifiedMethodName.indexOf(".getMethod()") > 0) {
				String userAgent = null;
				try {
					userAgent = (String) object.getClass().getMethod("getHeader", java.lang.String.class).invoke(object,
							"User-Agent");
					if (userAgent == null || !userAgent.equals("JMeter")) {
						userAgent = (String) object.getClass().getMethod("getHeader", java.lang.String.class)
								.invoke(object, "BenchmarkTest00008");
						if (userAgent != null && userAgent.equals("verifyUserPassword('foo','bar')"))
							userAgent = "ZAPNULL";
						else
							userAgent = "ZAP";
					}
				} catch (Exception ex) {
				}
				long threadTag = Math.abs(new Random().nextLong());
				Thread currentThread = Thread.currentThread();
				int index = currentThread.getName().indexOf("#");
				if (index > 0) {
					String threadBasicName = currentThread.getName().substring(0, index + 1);
					currentThread.setName(threadBasicName + threadTag + ' ' + userAgent);
				} else
					currentThread.setName(currentThread.getName() + "#" + threadTag + ' ' + userAgent);
			}*/

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result) {
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

	public static class ModelViewMethodAdvice {

		public static FeatureExtractor featureExtractorSingleton = FeatureExtractor.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result, @Advice.Thrown Throwable thrown) {
			if (invocation.returns()) {
				if (thrown != null)
					invocation.update("NULL", true);
				else if (result == null)
					invocation.update("NULL", false);
				else
					invocation.update(result, false);
			} else if (thrown != null)
				invocation.update(null, true);
			else
				invocation.update(null, false);
			FeatureRecord featureRecord = featureExtractorSingleton.extract(invocation);
			if (featureRecord != null)
				featureExtractorSingleton.log(featureRecord);
		}
	}
}
