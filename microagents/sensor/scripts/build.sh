cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/immunizer-sensor-agent.jar ../scripts/manifest.mf
