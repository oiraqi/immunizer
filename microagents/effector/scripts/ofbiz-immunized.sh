cd ../../ofbiz-framework
java -javaagent:../immunizer-sensor/framework/build/libs/immunizer-sensor-agent.jar -javaagent:../immunizer-effector/framework/build/libs/immunizer-effector-agent.jar -Dswid=OFBIZ-v3.4 -Dconfig=../immunizer-sensor/config/ofbiz-config.json -jar build/libs/ofbiz.jar
