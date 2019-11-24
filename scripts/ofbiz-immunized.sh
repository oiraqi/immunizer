cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/ofbiz-immunizer-agent.jar ../scripts/manifest-ofbiz.mf
cd ../../ofbiz-framework
java -javaagent:../immune-apps/framework/build/libs/ofbiz-immunizer-agent.jar -Dmodels=../immune-apps/models/ofbiz -Dbuffer=100 -Dskip=5000 -jar build/libs/ofbiz.jar
