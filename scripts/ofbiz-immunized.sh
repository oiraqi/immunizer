cd ../../ofbiz-framework
java -javaagent:../immunizer/microagents/sensor/framework/build/libs/immunizer-sensor-agent.jar -javaagent:../immunizer/microagents/effector/framework/build/libs/immunizer-effector-agent.jar -Dconfig=../immunizer/config/ofbiz-config.json -jar build/libs/ofbiz.jar
