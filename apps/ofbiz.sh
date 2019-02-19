cd ../framework
javac *.java
jar cfm ofbiz-intercept-agent.jar ../apps/ofbiz-manifest.mf OFBizInterceptAgent*.class
mv ofbiz-intercept-agent.jar ../apps/ofbiz/
cd ../apps/ofbiz/
./gradlew ofbiz -PjvmArgs="-Xms1024M -Xmx2048M -Dmodels=../../models/ofbiz -Dbuffer=100 -Dskip=1000 --add-modules java.xml.ws -javaagent:ofbiz-intercept-agent.jar"
