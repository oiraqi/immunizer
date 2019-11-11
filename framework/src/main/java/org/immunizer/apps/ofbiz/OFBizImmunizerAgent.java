package org.immunizer.apps.ofbiz;

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

import org.immunizer.core.FeatureExtractor;
import org.immunizer.core.FeatureRecord;
import org.immunizer.core.Invocation;

public class OFBizImmunizerAgent {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Instrumenter launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."))
				.type(named("org.apache.ofbiz.webapp.control.ControlFilter")
						/**
						 * //These are (re)activated for general scenarios and efficiency evaluation
						 * nameStartsWith("org.apache.ofbiz.accounting.invoice.")
						 * .or(nameStartsWith("org.apache.ofbiz.accounting.payment."))
						 * .or(nameStartsWith("org.apache.ofbiz.accounting.util."))
						 */
						.or(named("org.apache.ofbiz.entity.datasource.GenericDAO"))) // enough for our effectiveness
																						// evaluation scenario (the
																						// invoice update form)
				.transform(new InterceptTransformer()).installOn(inst);
	}

	private static class InterceptTransformer implements Transformer {
		@Override
		public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder,
				final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {

			// .and(named("update")) is enough for our effectiveness evaluation scenario
			// (the invoice update form)
			// should keep just .method(isPublic()) for general scenarios and efficiency
			// evaluation
			return builder.method(isPublic().and(named("doFilter"))).intercept(Advice.to(ControllerMethodAdvice.class))
					.method(isPublic().and(named("update"))).intercept(Advice.to(ModelMethodAdvice.class));
		}
	}

	public static class ControllerMethodAdvice {

		public static FeatureExtractor featureExtractorSingleton = FeatureExtractor.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(
				/* @Advice.This Object object, */@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			String userAgent = null;
			try {
				userAgent = (String) params[0].getClass().getMethod("getHeader", java.lang.String.class)
						.invoke(params[0], "User-Agent");
				if (userAgent == null || !userAgent.equals("JMeter")) {
					userAgent = "ZAP"; /**
										 * Label it as true positive, just for automatic evaluation of intrusion/oulier
										 * detection results
										 */
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

			System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");
			System.out.println(currentThread.getName());
			System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY");

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result) {
			// Do nothing here
		}
	}

	public static class ModelMethodAdvice {

		public static FeatureExtractor featureExtractorSingleton = FeatureExtractor.getSingleton();

		@Advice.OnMethodEnter
		public static Invocation onEnter(@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			return new Invocation("1.0", fullyQualifiedMethodName, params);
		}

		@Advice.OnMethodExit(onThrowable = Throwable.class)
		public static void onExit(@Advice.Enter Invocation invocation,
				@Advice.Return(typing = Assigner.Typing.DYNAMIC) Object result, @Advice.Thrown Throwable thrown) {

			/**
			 * Only intercept relevant calls to
			 * org.apache.ofbiz.entity.datasource.GenericDAO.update Relevant calls are the
			 * ones that carry user provided data. Other calls to the update method are
			 * triggered by OFBiz for some system-level logging such as web stats.
			 */
			if (invocation.getFullyQualifiedMethodName().equals(
					"public int org.apache.ofbiz.entity.datasource.GenericDAO.update(org.apache.ofbiz.entity.GenericEntity) throws org.apache.ofbiz.entity.GenericEntityException")) {
				boolean relevant = false;
				// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX");
				for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
					// System.out.println(ste);
					if (ste.getClassName().equals("org.apache.ofbiz.minilang.SimpleMethod")) {
						relevant = true;
						break;
					}
				}
				// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXX");
				if (!relevant)
					return;
			}
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
			FeatureRecord featureRecord = featureExtractorSingleton.extract(invocation);
			if (featureRecord != null)
				featureExtractorSingleton.log(featureRecord);
		}
	}
}
