cd ../framework
./gradlew agentLibs
jar cfm ./build/libs/immunizer-effector-agent.jar ../scripts/manifest.mf
