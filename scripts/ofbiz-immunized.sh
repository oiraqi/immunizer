cd ../framework
./gradlew agentLibs
cd build/classes/java/main
jar cfm ../../../libs/ofbiz-immunizer-agent.jar ../../../../../scripts/manifest-ofbiz.mf org/immunizer/apps/ofbiz/*.class
cd ../../../../../../ofbiz-framework
java -javaagent:../immune-apps/framework/build/libs/ofbiz-immunizer-agent.jar -Dmodels=../immune-apps/models/ofbiz -Dbuffer=100 -Dskip=5000 -jar build/libs/ofbiz.jar
