package org.immunizer.microagents.sensor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.instrument.Instrumentation;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Extendable;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Narrowable;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher.Junction;

import static net.bytebuddy.matcher.ElementMatchers.*;

import java.util.Random;

import com.google.gson.Gson;

public class Sensor {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Sensor Microagent Launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		AgentBuilder builder = new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."));

		/*
		 * builder.type(named("org.apache.ofbiz.webapp.control.ControlFilter"))
		 * .transform(new InterceptTransformer()).installOn(inst);
		 */
		try {
			Config config = getConfig();
			for (String ignore : config.ignore) {
				builder = builder.ignore(nameStartsWith(ignore + '.'));
			}
			Extendable extendable = null;
			Narrowable narrowable = null;
			for (String pkg : config.apply.packages) {				
				if (narrowable == null) {
					narrowable = builder.type(nameStartsWith(pkg + '.'));
				} else {
					narrowable = narrowable.or(nameStartsWith(pkg + '.'));
				}				
			}
			if (narrowable != null) {
				extendable = narrowable.transform(new MethodTransformer(any()));
			}
			for (Config.Apply.Class clazz : config.apply.classes) {
				Junction<? super MethodDescription> any = any(), matcher = any;
				for (Config.Apply.Class.Method method : clazz.methods) {
					Junction<? super MethodDescription> mtc = named(method.name);
					if (method.parameters != 0) {
						mtc = mtc.and(takesArguments(method.parameters));
					}
					if (matcher == any) {
						matcher = matcher.and(mtc);
					} else {
						matcher = matcher.or(mtc);
					}
				}
				if (extendable == null) {
					extendable = builder.type(named(clazz.name)).transform(new MethodTransformer(matcher));
				} else {
					extendable = extendable.type(named(clazz.name)).transform(new MethodTransformer(matcher));
				}
			}
			if (extendable != null) {
				extendable.installOn(inst);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static Config getConfig() throws Exception {
		String configPath = System.getProperty("config");
		BufferedReader br = new BufferedReader(new FileReader(configPath));
		String line = null;
		StringBuffer buffer = new StringBuffer();
		while ((line = br.readLine()) != null)
			buffer.append(line);
		br.close();

		Gson gson = new Gson();
		Config config = gson.fromJson(new String(buffer), Config.class);
		System.setProperty("swid", config.swid);
		System.setProperty("iid", config.iid);
		return config;
	}

	private static class Config {
		public String swid;
		public String iid;
		public String[] ignore = {};
		public Apply apply;

		public static class Apply {
			public String[] packages = {};
			public Class[] classes = {};

			public class Class {
				public String name;
				public Method[] methods = {};

				public class Method {
					public String name;
					public int parameters = 0;
				}
			}
		}
	}

	public static class ControllerMethodAdvice {

		@Advice.OnMethodEnter
		public static Invocation onEnter(
				/* @Advice.This Object object, */@Advice.Origin String fullyQualifiedMethodName,
				@Advice.AllArguments Object[] params) {

			String userAgent = null;
			String type = "Genuine";
			try {
				userAgent = (String) params[0].getClass().getMethod("getHeader", java.lang.String.class)
						.invoke(params[0], "User-Agent");
				if (userAgent == null || !userAgent.equals("JMeter")) {
					type = "Malicious"; /**
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
				currentThread.setName(threadBasicName + threadTag + ' ' + type);
			} else
				currentThread.setName(currentThread.getName() + "#" + threadTag + ' ' + type);

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
}
