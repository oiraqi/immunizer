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
						matcher = mtc;
					} else {
						matcher = matcher.or(mtc);
					}
				}
				if (extendable == null) {
					extendable = builder.type(named(clazz.name)).transform(new MethodTransformer(matcher, clazz.entry));
				} else {
					extendable = extendable.type(named(clazz.name)).transform(new MethodTransformer(matcher, clazz.entry));
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
		if(config.cxid == null) {
			config.cxid = "1";
		}
		System.setProperty("cxid", config.cxid);
		return config;
	}

	private static class Config {
		public String swid;
		public String cxid;
		public String[] ignore = {};
		public Apply apply;

		public static class Apply {
			public String[] packages = {};
			public Class[] classes = {};

			public class Class {
				public String name;
				public Method[] methods = {};
				public boolean entry = false;

				public class Method {
					public String name;
					public int parameters = 0;
				}
			}
		}
	}
}
