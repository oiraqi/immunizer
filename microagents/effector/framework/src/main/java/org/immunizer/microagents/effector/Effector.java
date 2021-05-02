package org.immunizer.microagents.effector;

import java.lang.instrument.Instrumentation;
import java.time.Duration;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Extendable;
import net.bytebuddy.agent.builder.AgentBuilder.Identified.Narrowable;

import static net.bytebuddy.matcher.ElementMatchers.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class Effector {
	public static void premain(String arg, Instrumentation inst) throws Exception {
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		System.out.println("Effector Microagent Launched!");
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		
		AlarmManager alarmManager = AlarmManager.getSingleton();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				AlarmConsumer consumer = new AlarmConsumer();
				ConsumerRecords<String, Alarm> records;
				while (true) {
					records = consumer.poll(Duration.ofSeconds(60));
					AgentBuilder builder = new AgentBuilder.Default().ignore(nameStartsWith("net.bytebuddy."));
					Extendable extendable = null;
					Narrowable narrowable = null;
					for (ConsumerRecord<String, Alarm> record : records) {
						if (!alarmManager.isProtected(record.value().getCallStackId())) {
							if (narrowable == null) {
								narrowable = builder.type(named(record.value().getFullyQualifiedClassName()));
							} else {
								narrowable = narrowable.or(named(record.value().getFullyQualifiedClassName()));
							}
							if (extendable == null) {
								extendable = narrowable.transform(new MethodTransformer(named(record.value().getName())));
							} else {
								extendable = extendable.transform(new MethodTransformer(named(record.value().getName())));
							}
							
						}
						alarmManager.addAlarm(record.value());
					}
					if (extendable != null) {
						extendable.installOn(inst);
					}
				}
			}
		}).start();
	}
}
